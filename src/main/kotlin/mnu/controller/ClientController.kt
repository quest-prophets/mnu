package mnu.controller

import mnu.model.enums.RequestStatus
import mnu.model.enums.ShoppingCartStatus
import mnu.model.request.PurchaseRequest
import mnu.model.request.Request
import mnu.model.shop.ShoppingCart
import mnu.model.shop.ShoppingCartItem
import mnu.repository.TransportRepository
import mnu.repository.WeaponRepository
import mnu.repository.request.PurchaseRequestRepository
import mnu.repository.shop.ShoppingCartItemRepository
import mnu.repository.shop.ShoppingCartRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.security.Principal

@Controller
@RequestMapping("/client")
class ClientController (
    val weaponRepository: WeaponRepository,
    val transportRepository: TransportRepository,
    val shoppingCartItemRepository: ShoppingCartItemRepository,
    val shoppingCartRepository: ShoppingCartRepository,
    val purchaseRequestRepository: PurchaseRequestRepository
) : ApplicationController() {

    @GetMapping("/shop")
    fun clientsShop(model: Model, principal: Principal): String {
        val currentClient = clientRepository?.findByUserId(userRepository?.findByLogin(principal.name)!!.id!!)
        val allAvailableWeapons = weaponRepository.findAllByQuantityGreaterThanEqual(0)
        val allAvailableTransport = transportRepository.findAllByQuantityGreaterThanEqual(0)
        model.addAttribute("user", currentClient)
        model.addAttribute("weapons", allAvailableWeapons)
        model.addAttribute("transport", allAvailableTransport)
        return "customers/customer__shop.html"
    }

    enum class CartItemType { WEAPON, TRANSPORT }
    data class CartItem(val type: CartItemType, var id: Long, var quantity: Long)

    @PostMapping("/cart/modify")
    fun modifyCart(@RequestBody cartItem: CartItem, principal: Principal, redirect: RedirectAttributes): String {
        val currentClient = clientRepository?.findByUserId(userRepository?.findByLogin(principal.name)!!.id!!)!!
        val currentCreatingCart = when {
            shoppingCartRepository.findAllByClientAndStatus(currentClient, ShoppingCartStatus.CREATING) != null ->
                shoppingCartRepository.findAllByClientAndStatus(currentClient, ShoppingCartStatus.CREATING)!![0]
            else -> ShoppingCart(currentClient).apply { this.status = ShoppingCartStatus.CREATING }
        }

        shoppingCartRepository.save(currentCreatingCart)

        var newShoppingCartItem = ShoppingCartItem()
        when (cartItem.type) {
            CartItemType.WEAPON -> {
                val possibleWeapon = weaponRepository.findById(cartItem.id)
                if (!possibleWeapon.isPresent) {
                    redirect.addFlashAttribute("error", "Such weapon does not exist.")
                    return "redirect:/client/shop"
                }
                val existingWeapon = possibleWeapon.get()
                if (existingWeapon.quantity == 0L) {
                    redirect.addFlashAttribute("error", "Out of stock. Check back later or choose another weapon.")
                    return "redirect:/client/shop"
                }
                val existingShoppingCartItemCheck = shoppingCartItemRepository.findByWeaponAndCart(existingWeapon, currentCreatingCart)
                if (existingShoppingCartItemCheck == null) {
                    if (cartItem.quantity <= 0L) {
                        redirect.addFlashAttribute("error", "Please enter a valid quantity for this item.")
                        return "redirect:/client/shop"
                    }
                    newShoppingCartItem.apply {
                        this.weapon = existingWeapon
                        this.weaponQuantity = cartItem.quantity
                    }
                } else {
                    if (cartItem.quantity <= 0L) {
                        shoppingCartItemRepository.delete(existingShoppingCartItemCheck)
                        redirect.addFlashAttribute("status", "Item removed from cart.")
                        return "redirect:/client/shop"
                    }
                    existingShoppingCartItemCheck.weaponQuantity = cartItem.quantity
                    newShoppingCartItem = existingShoppingCartItemCheck
                }
            }

            CartItemType.TRANSPORT -> {
                val possibleTransport = transportRepository.findById(cartItem.id)
                if (!possibleTransport.isPresent) {
                    redirect.addFlashAttribute("error", "Such weapon does not exist.")
                    return "redirect:/client/shop"
                }
                val existingTransport = possibleTransport.get()
                if (existingTransport.quantity == 0L) {
                    redirect.addFlashAttribute("error", "Out of stock. Check back later or choose another weapon.")
                    return "redirect:/client/shop"
                }
                val existingShoppingCartItemCheck = shoppingCartItemRepository.findByTransportAndCart(existingTransport, currentCreatingCart)
                if (existingShoppingCartItemCheck == null) {
                    if (cartItem.quantity <= 0L) {
                        redirect.addFlashAttribute("error", "Please enter a valid quantity for this item.")
                        return "redirect:/client/shop"
                    }
                    newShoppingCartItem.apply {
                        this.transport = existingTransport
                        this.transportQuantity = cartItem.quantity
                    }
                } else {
                    if (cartItem.quantity <= 0L) {
                        shoppingCartItemRepository.delete(existingShoppingCartItemCheck)
                        redirect.addFlashAttribute("status", "Item removed from cart.")
                        return "redirect:/client/shop"
                    }
                    existingShoppingCartItemCheck.transportQuantity = existingShoppingCartItemCheck.transportQuantity!! + cartItem.quantity
                    newShoppingCartItem = existingShoppingCartItemCheck
                }
            }
        }

        shoppingCartItemRepository.save(newShoppingCartItem.apply { this.cart = currentCreatingCart })

        redirect.addFlashAttribute("status", "Added to cart!")
        return "redirect:/client/shop"
    }

    @PostMapping("/cart/sendRequest")
    fun sendPurchaseRequest(principal: Principal, redirect: RedirectAttributes) : String {
        val newRequest = Request().apply { this.status = RequestStatus.PENDING }
        val currentUser = userRepository?.findByLogin(principal.name)!!
        val currentClient = clientRepository?.findByUserId(currentUser.id!!)!!
        val currentCreatingCart = when (shoppingCartRepository.findAllByClientAndStatus(currentClient, ShoppingCartStatus.CREATING)) {
            null -> {
                redirect.addFlashAttribute("error", "You have no carts in creation.")
                return "redirect:/client/shop"
            }
            else -> {
                shoppingCartRepository.findAllByClientAndStatus(currentClient, ShoppingCartStatus.CREATING)!![0]
            }
        }

        if (currentCreatingCart.items == null || currentCreatingCart.items!!.size == 0) {
            redirect.addFlashAttribute("error", "You have no items in your cart.")
            return "redirect:/client/shop"
        }

        val cartItems = currentCreatingCart.items

        cartItems!!.forEach {
            if (it.weapon != null) {
                if (it.weapon!!.quantity < it.weaponQuantity!!) {
                    redirect.addFlashAttribute("error",
                            "No sufficient \"${it.weapon!!.name}\" weapons, request cannot be satisfied.")
                    return "redirect:/client/cart"
                }
                it.weapon!!.quantity -= it.weaponQuantity!!
                weaponRepository.save(it.weapon!!)
            }
            if(it.transport != null) {
                if(it.transport!!.quantity < it.transportQuantity!!) {
                    redirect.addFlashAttribute("error",
                        "No sufficient \"${it.transport!!.name}\" transport, request cannot be satisfied.")
                    return "redirect:/client/cart"
                }
                it.transport!!.quantity -= it.transportQuantity!!
                transportRepository.save(it.transport!!)
            }
        }

        currentCreatingCart.status = ShoppingCartStatus.REQUESTED
        shoppingCartRepository.save(currentCreatingCart)
        purchaseRequestRepository.save(PurchaseRequest(currentUser, currentCreatingCart).apply { this.request = newRequest })

        redirect.addFlashAttribute("status", "Request sent. Await for your managing employee's decision.")
        return "redirect:/client/shop"
    }

}