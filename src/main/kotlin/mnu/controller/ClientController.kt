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

    data class CartModifyResponse(var isError: Boolean = false, var message: String = "")

    @PostMapping("/cart/updateQuantity")
    fun modifyQuantity(@RequestParam("itemId") itemId: Long,
                       @RequestParam("newQuantity") newQuantity: Long,
                       principal: Principal, redirect: RedirectAttributes): String {
        val currentUser = userRepository?.findByLogin(principal.name)!!
        val shoppingCartItem = shoppingCartItemRepository.findByIdAndCartUserIdAndCartStatus(itemId, currentUser.id!!, ShoppingCartStatus.CREATING)
        if (shoppingCartItem != null && newQuantity <= 0L) {
            shoppingCartItem.cart = null
            shoppingCartItemRepository.delete(shoppingCartItem)

            redirect.addFlashAttribute("status", "Item deleted.")
            return "redirect:/client/cart"
        }

        if (shoppingCartItem != null && newQuantity > 0) {
            shoppingCartItemRepository.save(shoppingCartItem.apply {
                if (this.weapon != null)
                    this.quantity = newQuantity
                else if (this.transport != null)
                    this.quantity = newQuantity
            })

            redirect.addFlashAttribute("status", "Quantity updated.")
            return "redirect:/client/cart"
        }

        redirect.addFlashAttribute("error", "Invalid request.")
        return "redirect:/client/cart"
    }

    @PostMapping("/cart/modifyAjax")
    @ResponseBody
    fun modifyCartAjax(@RequestBody cartItem: CartItem, principal: Principal, redirect: RedirectAttributes): CartModifyResponse {
        val currentUser = userRepository?.findByLogin(principal.name)!!
        val currentClient = clientRepository?.findByUserId(currentUser.id!!)!!
        val possibleCart = shoppingCartRepository.findAllByUserAndStatus(currentUser, ShoppingCartStatus.CREATING)
        val currentCreatingCart = when {
            possibleCart != null && possibleCart.isNotEmpty() ->
                possibleCart[0]
            else -> ShoppingCart(currentUser).apply {
                this.status = ShoppingCartStatus.CREATING
                this.items = mutableListOf()
            }
        }

        shoppingCartRepository.save(currentCreatingCart)

        var newShoppingCartItem = ShoppingCartItem()
        when (cartItem.type) {
            CartItemType.WEAPON -> {
                val possibleWeapon = weaponRepository.findById(cartItem.id)
                if (!possibleWeapon.isPresent) {
                    return CartModifyResponse(isError = true, message = "Such weapon does not exist.")
                }
                val existingWeapon = possibleWeapon.get()
                if (existingWeapon.quantity == 0L) {
                    return CartModifyResponse(isError = true, message = "Out of stock. Check back later or choose another weapon.")
                }
                val existingShoppingCartItemCheck = shoppingCartItemRepository.findByWeaponAndCart(existingWeapon, currentCreatingCart)
                if (existingShoppingCartItemCheck == null) {
                    if (cartItem.quantity <= 0L) {
                        return CartModifyResponse(isError = true, message = "Please enter a valid quantity for this item.")
                    }
                    newShoppingCartItem.apply {
                        this.weapon = existingWeapon
                        this.quantity = cartItem.quantity
                    }
                } else {
                    if (cartItem.quantity <= 0L) {
                        existingShoppingCartItemCheck.cart = null
                        shoppingCartItemRepository.delete(existingShoppingCartItemCheck)
                        return CartModifyResponse(isError = false, message = "Item removed from cart.")
                    }
                    existingShoppingCartItemCheck.quantity = cartItem.quantity
                    newShoppingCartItem = existingShoppingCartItemCheck
                }
            }

            CartItemType.TRANSPORT -> {
                val possibleTransport = transportRepository.findById(cartItem.id)
                if (!possibleTransport.isPresent) {
                    return CartModifyResponse(isError = true, message = "Such transport does not exist.")
                }
                val existingTransport = possibleTransport.get()
                if (existingTransport.quantity == 0L) {
                    return CartModifyResponse(isError = true, message = "Out of stock. Check back later or choose another transport.")
                }
                val existingShoppingCartItemCheck = shoppingCartItemRepository.findByTransportAndCart(existingTransport, currentCreatingCart)
                if (existingShoppingCartItemCheck == null) {
                    if (cartItem.quantity <= 0L) {
                        return CartModifyResponse(isError = true, message = "Please enter a valid quantity for this item.")
                    }
                    newShoppingCartItem.apply {
                        this.transport = existingTransport
                        this.quantity = cartItem.quantity
                    }
                } else {
                    if (cartItem.quantity <= 0L) {
                        existingShoppingCartItemCheck.cart = null
                        shoppingCartItemRepository.delete(existingShoppingCartItemCheck)
                        return CartModifyResponse(isError = false, message = "Item removed from cart.")
                    }
                    existingShoppingCartItemCheck.quantity = cartItem.quantity
                    newShoppingCartItem = existingShoppingCartItemCheck
                }
            }
        }

        shoppingCartItemRepository.save(newShoppingCartItem.apply { this.cart = currentCreatingCart })

        return CartModifyResponse(isError = false, message = "Added to cart!")
    }

    @PostMapping("/cart/sendRequest")
    fun sendPurchaseRequest(principal: Principal, redirect: RedirectAttributes) : String {
        val newRequest = Request().apply { this.status = RequestStatus.PENDING }
        val currentUser = userRepository?.findByLogin(principal.name)!!
        val possibleCart = shoppingCartRepository.findAllByUserAndStatus(currentUser, ShoppingCartStatus.CREATING)
        val currentCreatingCart = when {
            possibleCart != null && possibleCart.isNotEmpty() ->
                possibleCart[0]
            else -> ShoppingCart(currentUser).apply {
                this.status = ShoppingCartStatus.CREATING
                this.items = mutableListOf()
            }
        }

        if (currentCreatingCart.items == null || currentCreatingCart.items!!.size == 0) {
            redirect.addFlashAttribute("error", "You have no items in your cart.")
            return "redirect:/client/shop"
        }

        val cartItems = currentCreatingCart.items

        cartItems!!.forEach {
            if (it.weapon != null) {
                if (it.weapon!!.quantity < it.quantity!!) {
                    redirect.addFlashAttribute("error",
                            "No sufficient \"${it.weapon!!.name}\" weapons, request cannot be satisfied.")
                    return "redirect:/client/cart"
                }
                it.weapon!!.quantity -= it.quantity!!
                weaponRepository.save(it.weapon!!)
            }
            if(it.transport != null) {
                if(it.transport!!.quantity < it.quantity!!) {
                    redirect.addFlashAttribute("error",
                        "No sufficient \"${it.transport!!.name}\" transport, request cannot be satisfied.")
                    return "redirect:/client/cart"
                }
                it.transport!!.quantity -= it.quantity!!
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