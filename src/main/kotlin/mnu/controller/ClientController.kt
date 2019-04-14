package mnu.controller

import mnu.model.Transport
import mnu.model.Weapon
import mnu.model.enums.RequestStatus
import mnu.model.enums.ShoppingCartStatus
import mnu.model.enums.TransportType
import mnu.model.enums.WeaponType
import mnu.model.request.PurchaseRequest
import mnu.model.request.Request
import mnu.model.shop.ShoppingCart
import mnu.model.shop.ShoppingCartItem
import mnu.repository.TransportRepository
import mnu.repository.WeaponRepository
import mnu.repository.request.PurchaseRequestRepository
import mnu.repository.shop.ShoppingCartItemRepository
import mnu.repository.shop.ShoppingCartRepository
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
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

    @GetMapping("/shop/{category}")
    fun clientsShop(@PathVariable("category") category: String,
                    @RequestParam(required = false) name: String?,
                    @RequestParam(required = false) type: String?,
                    @RequestParam(required = false) sort: String?,
                    model: Model, principal: Principal, redirect: RedirectAttributes): String {
        val currentClient = clientRepository?.findByUserId(userRepository?.findByLogin(principal.name)!!.id!!)
        model.addAttribute("user", currentClient)

        val productType = if (category == "weapon") WeaponType.fromClient(type) else TransportType.fromClient(type)
        val productSort = when (sort) {
            "price_asc" -> Sort(Sort.Direction.ASC, "price")
            "price_desc" -> Sort(Sort.Direction.DESC, "price")
            else -> Sort(Sort.Direction.ASC, "id")
        }

        val items: List<Any> = when {
            category == "weapon" && name != null && productType != null ->
                weaponRepository.findAllByNameIgnoreCaseContainingAndTypeAndQuantityGreaterThan(name, productType as WeaponType, 0, productSort)
            category == "transport" && name != null && productType != null ->
                transportRepository.findAllByNameIgnoreCaseContainingAndTypeAndQuantityGreaterThan(name, productType as TransportType, 0, productSort)
            category == "weapon" && name == null && productType != null ->
                weaponRepository.findAllByTypeAndQuantityGreaterThan(productType as WeaponType, 0, productSort)
            category == "transport" && name == null && productType != null ->
                transportRepository.findAllByTypeAndQuantityGreaterThan(productType as TransportType, 0, productSort)

            category == "weapon" && name != null && productType == null ->
                weaponRepository.findAllByNameIgnoreCaseContainingAndQuantityGreaterThan(name, 0, productSort)
            category == "transport" && name != null && productType == null ->
                transportRepository.findAllByNameIgnoreCaseContainingAndQuantityGreaterThan(name, 0, productSort)
            category == "weapon" && name == null && productType == null ->
                weaponRepository.findAllByQuantityGreaterThanEqual(1, productSort)
            category == "transport" && name == null && productType == null ->
                transportRepository.findAllByQuantityGreaterThanEqual(1, productSort)
            else -> {
                redirect.addFlashAttribute("error", "Such category filter does not exist.")
                return "redirect:/manufacturer/market/weapon"
            }
        }
        model.addAttribute("items", items)
        return "customers/customer__shop.html"
    }

    @GetMapping("/cart")
    fun saleCart(model: Model, principal: Principal) : String {
        val currentUser = userRepository?.findByLogin(principal.name)!!
        val possibleCart = shoppingCartRepository.findAllByUserAndStatus(currentUser, ShoppingCartStatus.CREATING)
        val usersCart = when {
            possibleCart != null && possibleCart.isNotEmpty() ->
                possibleCart[0]
            else -> ShoppingCart(currentUser).apply {
                this.status = ShoppingCartStatus.CREATING
                this.items = mutableListOf()
            }
        }
        model.addAttribute("cart_items", usersCart.items)
        return "manufacturers/manufacturer__cart.html"
    }

    enum class CartItemType { WEAPON, TRANSPORT }
    data class CartItem(val type: CartItemType, var id: Long, var quantity: Long)

    @PostMapping("/cart/modify")
    fun modifyCart(@RequestBody cartItem: CartItem, principal: Principal, redirect: RedirectAttributes): String {
        val currentUser = userRepository?.findByLogin(principal.name)!!
        val currentClient = clientRepository?.findByUserId(currentUser.id!!)!!
        val currentCreatingCart = when {
            shoppingCartRepository.findAllByUserAndStatus(currentUser, ShoppingCartStatus.CREATING) != null ->
                shoppingCartRepository.findAllByUserAndStatus(currentUser, ShoppingCartStatus.CREATING)!![0]
            else -> ShoppingCart(currentUser).apply { this.status = ShoppingCartStatus.CREATING }
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
                    redirect.addFlashAttribute("error", "Such transport does not exist.")
                    return "redirect:/client/shop"
                }
                val existingTransport = possibleTransport.get()
                if (existingTransport.quantity == 0L) {
                    redirect.addFlashAttribute("error", "Out of stock. Check back later or choose another transport.")
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
        val currentCreatingCart = when (shoppingCartRepository.findAllByUserAndStatus(currentUser, ShoppingCartStatus.CREATING)) {
            null -> {
                redirect.addFlashAttribute("error", "You have no carts in creation.")
                return "redirect:/client/shop"
            }
            else -> {
                shoppingCartRepository.findAllByUserAndStatus(currentUser, ShoppingCartStatus.CREATING)!![0]
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