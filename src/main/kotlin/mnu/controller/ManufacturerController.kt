package mnu.controller

import mnu.form.NewProductForm
import mnu.form.NewVacancyForm
import mnu.model.Transport
import mnu.model.Weapon
import mnu.model.enums.*
import mnu.model.request.*
import mnu.model.shop.ShoppingCart
import mnu.model.shop.ShoppingCartItem
import mnu.repository.TransportRepository
import mnu.repository.WeaponRepository
import mnu.repository.request.*
import mnu.repository.shop.*
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.security.Principal

@Controller
@RequestMapping("/manufacturer")
class ManufacturerController (
    val shoppingCartItemRepository: ShoppingCartItemRepository,
    val shoppingCartRepository: ShoppingCartRepository,
    val purchaseRequestRepository: PurchaseRequestRepository,
    val newWeaponRequestRepository: NewWeaponRequestRepository,
    val newTransportRequestRepository: NewTransportRequestRepository,
    val newVacancyRequestRepository: NewVacancyRequestRepository,
    val transportRepository: TransportRepository,
    val weaponRepository: WeaponRepository
) : ApplicationController() {
    @GetMapping("/market/{category}")
    fun market(@PathVariable("category") category: String,
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
                weaponRepository.findAllByNameIgnoreCaseContainingAndType(name, productType as WeaponType, productSort)
            category == "transport" && name != null && productType != null ->
                transportRepository.findAllByNameIgnoreCaseContainingAndType(name, productType as TransportType, productSort)
            category == "weapon" && name == null && productType != null ->
                weaponRepository.findAllByType(productType as WeaponType, productSort)
            category == "transport" && name == null && productType != null ->
                transportRepository.findAllByType(productType as TransportType, productSort)

            category == "weapon" && name != null && productType == null ->
                weaponRepository.findAllByNameIgnoreCaseContaining(name, productSort)
            category == "transport" && name != null && productType == null ->
                transportRepository.findAllByNameIgnoreCaseContaining(name, productSort)
            category == "weapon" && name == null && productType == null ->
                weaponRepository.findAll(productSort)
            category == "transport" && name == null && productType == null ->
                transportRepository.findAll(productSort)
            else -> {
                redirect.addFlashAttribute("error", "Such category filter does not exist.")
                return "redirect:/manufacturer/market/weapon"
            }
        }
        model.addAttribute("items", items)
        return "manufacturers/manufacturer__market.html"
    }

    @GetMapping("/newProduct")
    fun newProduct(model: Model): String {
        model.addAttribute("form", NewProductForm())
        return "manufacturers/manufacturer__new-product.html"
    }

    @GetMapping("/newVacancy")
    fun newVacancy(model: Model): String {
        model.addAttribute("form", NewVacancyForm())
        return "manufacturers/manufacturer__new-vacancy.html"
    }

    @PostMapping("/newProduct")
    fun requestNewProduct (@ModelAttribute form: NewProductForm, redirect: RedirectAttributes, principal: Principal) : String {
        val user = userRepository?.findByLogin(principal.name)
        val manufacturer = clientRepository?.findByUserId(user!!.id!!)
        val newRequest = Request().apply { this.status = RequestStatus.PENDING }
        val productType = when (form.type) {
            "melee" -> WeaponType.MELEE
            "pistol" -> WeaponType.PISTOL
            "submachine_gun" -> WeaponType.SUBMACHINE_GUN
            "assault_rifle" -> WeaponType.ASSAULT_RIFLE
            "light_machine_gun" -> WeaponType.LIGHT_MACHINE_GUN
            "sniper_rifle" -> WeaponType.SNIPER_RIFLE
            "alien" -> WeaponType.ALIEN
            "land" -> TransportType.LAND
            "air" -> TransportType.AIR
            else -> {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Such product type does not exist.")
                return "redirect:/manufacturer/newProduct"
            }
        }

        when {
            form.accessLvl.toInt() < 1 || form.accessLvl.toInt() > 10 -> {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Please enter product access level between 1-10.")
                return "redirect:/manufacturer/newProduct"
            }
            form.price.toDouble() < 1 -> {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Please enter a valid price for this product.")
                return "redirect:/manufacturer/newProduct"
            }
        }

        val newProductRequest = when (productType) {
            TransportType.AIR, TransportType.LAND -> {
                val type = when (productType) {
                    TransportType.AIR -> TransportType.AIR
                    TransportType.LAND -> TransportType.LAND
                    else -> {
                        redirect.addFlashAttribute("form", form)
                        redirect.addFlashAttribute("error", "Such transport type does not exist.")
                        return "redirect:/manufacturer/newProduct"
                    }
                }
                NewTransportRequest(form.name, type, form.description, 0, form.accessLvl.toInt(), form.price.toDouble(), manufacturer)
            }
            else -> {
                val type = when (productType) {
                    WeaponType.MELEE -> WeaponType.MELEE
                    WeaponType.PISTOL -> WeaponType.PISTOL
                    WeaponType.SUBMACHINE_GUN -> WeaponType.SUBMACHINE_GUN
                    WeaponType.ASSAULT_RIFLE -> WeaponType.ASSAULT_RIFLE
                    WeaponType.LIGHT_MACHINE_GUN -> WeaponType.LIGHT_MACHINE_GUN
                    WeaponType.SNIPER_RIFLE -> WeaponType.SNIPER_RIFLE
                    WeaponType.ALIEN -> WeaponType.ALIEN
                    else -> {
                        redirect.addFlashAttribute("form", form)
                        redirect.addFlashAttribute("error", "Such transport type does not exist.")
                        return "redirect:/manufacturer/newProduct"
                    }
                }
                NewWeaponRequest(form.name, type, form.description, 0, form.accessLvl.toInt(), form.price.toDouble(), user)
            }
        }

        if (newProductRequest is NewWeaponRequest)
            newWeaponRequestRepository.save(newProductRequest.apply { this.request = newRequest })
        if (newProductRequest is NewTransportRequest)
            newTransportRequestRepository.save(newProductRequest.apply { this.request = newRequest })

        redirect.addFlashAttribute("status", "Request submitted. Await for administrator's decision.")
        return "redirect:/manufacturer/market"
    }

    @PostMapping("/newVacancy")
    fun addVacancy(@ModelAttribute form: NewVacancyForm, redirect: RedirectAttributes, principal: Principal): String {
        val user = userRepository?.findByLogin(principal.name)
        val manufacturer = clientRepository?.findByUserId(user!!.id!!)
        val newRequest = Request().apply { this.status = RequestStatus.PENDING }

        if (form.title == "" || form.salary == "" || form.requiredKarma == "" || form.workHoursPerWeek == "" || form.vacantPlaces == "") {
            redirect.addFlashAttribute("form", form)
            redirect.addFlashAttribute("error", "One of the fields isn't filled.")
            return "redirect:/manufacturer/newVacancies"
        }
        val newVacancyRequest =
            NewVacancyRequest(form.title, form.salary.toLong(), form.requiredKarma.toLong(),
                form.workHoursPerWeek.toInt(), form.vacantPlaces.toLong(), manufacturer)
                .apply { this.request = newRequest }

        newVacancyRequestRepository.save(newVacancyRequest)

        redirect.addFlashAttribute("form", form)
        redirect.addFlashAttribute("status", "Request submitted. Await for administrator's decision.")
        return "redirect:/manufacturer/market"
    }

    enum class CartItemType { WEAPON, TRANSPORT }
    data class CartItem(val type: CartItemType, var id: Long, var quantity: Long)

    data class CartModifyResponse(var isError: Boolean = false, var message: String = "")

    @PostMapping("/cart/modifyAjax")
    @ResponseBody
    fun modifyCartAjax(@RequestBody cartItem: CartItem, principal: Principal, redirect: RedirectAttributes): CartModifyResponse {
        val currentUser = userRepository?.findByLogin(principal.name)!!
        val currentClient = clientRepository?.findByUserId(currentUser.id!!)!!
        val possibleCart = shoppingCartRepository.findAllByUserAndStatus(currentUser, ShoppingCartStatus.CREATING)
        val currentCreatingCart = when {
            possibleCart != null && possibleCart.isNotEmpty() ->
                possibleCart[0]
            else -> ShoppingCart(currentUser).apply { this.status = ShoppingCartStatus.CREATING }
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
                val existingShoppingCartItemCheck = shoppingCartItemRepository.findByWeaponAndCart(existingWeapon, currentCreatingCart)
                if (existingShoppingCartItemCheck == null) {
                    if (cartItem.quantity <= 0L) {
                        return CartModifyResponse(isError = true, message = "Please enter a valid quantity for this item.")
                    }
                    newShoppingCartItem.apply {
                        this.weapon = existingWeapon
                        this.weaponQuantity = cartItem.quantity
                    }
                } else {
                    if (cartItem.quantity <= 0L) {
                        shoppingCartItemRepository.delete(existingShoppingCartItemCheck)
                        return CartModifyResponse(isError = false, message = "Item removed from cart.")
                    }
                    existingShoppingCartItemCheck.weaponQuantity = cartItem.quantity
                    newShoppingCartItem = existingShoppingCartItemCheck
                }
            }

            CartItemType.TRANSPORT -> {
                val possibleTransport = transportRepository.findById(cartItem.id)
                if (!possibleTransport.isPresent) {
                    return CartModifyResponse(isError = true, message = "Such weapon does not exist.")
                }
                val existingTransport = possibleTransport.get()
                val existingShoppingCartItemCheck = shoppingCartItemRepository.findByTransportAndCart(existingTransport, currentCreatingCart)
                if (existingShoppingCartItemCheck == null) {
                    if (cartItem.quantity <= 0L) {
                        return CartModifyResponse(isError = true, message = "Please enter a valid quantity for this item.")
                    }
                    newShoppingCartItem.apply {
                        this.transport = existingTransport
                        this.transportQuantity = cartItem.quantity
                    }
                } else {
                    if (cartItem.quantity <= 0L) {
                        shoppingCartItemRepository.delete(existingShoppingCartItemCheck)
                        return CartModifyResponse(isError = false, message = "Item removed from cart.")
                    }
                    existingShoppingCartItemCheck.transportQuantity = existingShoppingCartItemCheck.transportQuantity!! + cartItem.quantity
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
        val currentCreatingCart = when (shoppingCartRepository.findAllByUserAndStatus(currentUser, ShoppingCartStatus.CREATING)) {
            null -> {
                redirect.addFlashAttribute("error", "You have no carts in creation.")
                return "redirect:/manufacturer/market"
            }
            else -> {
                shoppingCartRepository.findAllByUserAndStatus(currentUser, ShoppingCartStatus.CREATING)!![0]
            }
        }

        if (currentCreatingCart.items == null || currentCreatingCart.items!!.size == 0) {
            redirect.addFlashAttribute("error", "You have no items in your cart.")
            return "redirect:/manufacturer/market"
        }

        val cartItems = currentCreatingCart.items

        cartItems!!.forEach {
            if (it.weapon != null) {
                it.weapon!!.quantity += it.weaponQuantity!!
                weaponRepository.save(it.weapon!!)
            }
            if(it.transport != null) {
                it.transport!!.quantity += it.transportQuantity!!
                transportRepository.save(it.transport!!)
            }
        }

        currentCreatingCart.status = ShoppingCartStatus.REQUESTED
        shoppingCartRepository.save(currentCreatingCart)
        purchaseRequestRepository.save(PurchaseRequest(currentUser, currentCreatingCart).apply { this.request = newRequest })

        redirect.addFlashAttribute("status", "Request sent. Await for your managing employee's decision.")
        return "redirect:/manufacturer/market"
    }
}