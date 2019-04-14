package mnu.controller

import mnu.form.NewPasswordForm
import mnu.model.Transport
import mnu.model.Vacancy
import mnu.model.Weapon
import mnu.model.enums.RequestStatus
import mnu.model.enums.ShoppingCartStatus
import mnu.model.enums.TransportType
import mnu.model.enums.WeaponType
import mnu.model.request.PurchaseRequest
import mnu.model.request.Request
import mnu.model.request.VacancyApplicationRequest
import mnu.model.shop.ShoppingCart
import mnu.model.shop.ShoppingCartItem
import mnu.repository.*
import mnu.repository.request.PurchaseRequestRepository
import mnu.repository.request.RequestRepository
import mnu.repository.request.VacancyApplicationRequestRepository
import mnu.repository.shop.ShoppingCartItemRepository
import mnu.repository.shop.ShoppingCartRepository
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.security.Principal

@Controller
@RequestMapping("/prawn")
class PrawnController (
    val districtIncidentRepository: DistrictIncidentRepository,
    val districtHouseRepository: DistrictHouseRepository,
    val requestRepository: RequestRepository,
    val vacancyRepository: VacancyRepository,
    val vacancyApplicationRequestRepository: VacancyApplicationRequestRepository,
    val weaponRepository: WeaponRepository,
    val transportRepository: TransportRepository,
    val purchaseRequestRepository: PurchaseRequestRepository,
    val shoppingCartRepository: ShoppingCartRepository,
    val shoppingCartItemRepository: ShoppingCartItemRepository
) : ApplicationController() {

    @GetMapping("/main")
    fun prawnMain(model: Model, principal: Principal): String {
        val currentPrawn = prawnRepository?.findByUserId(userRepository?.findByLogin(principal.name)!!.id!!)
        model.addAttribute("user", currentPrawn)
        return "prawns/prawn__main.html"
    }

    @GetMapping("/shop/{category}")
    fun prawnShop(@PathVariable("category") category: String,
                  @RequestParam(required = false) name: String?,
                  @RequestParam(required = false) type: String?,
                  @RequestParam(required = false) sort: String?,
                  model: Model, principal: Principal, redirect: RedirectAttributes): String {
        val currentPrawn = prawnRepository?.findByUserId(userRepository?.findByLogin(principal.name)!!.id!!)!!
        if (currentPrawn.karma < 5000) {
            redirect.addFlashAttribute("error", "You have no permission to buy our products.")
            return "redirect:/prawn/main"
        }

        model.addAttribute("user", currentPrawn)

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
        return "prawns/prawn__shop.html"
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

    @GetMapping("/profile")
    fun prawnProfile(model: Model, principal: Principal): String {
        val currentPrawn = prawnRepository?.findByUserId(userRepository?.findByLogin(principal.name)!!.id!!)
        model.addAttribute("user", currentPrawn)
        model.addAttribute("form", NewPasswordForm())
        return "prawns/prawn__profile.html"
    }

    @GetMapping("/vacancies")
    fun prawnVacancies(@RequestParam (required = false) sort: String?, model: Model, principal: Principal) : String {
        val currentPrawn = prawnRepository?.findByUserId(userRepository?.findByLogin(principal.name)!!.id!!)!!
        val allVacancyRequests = vacancyApplicationRequestRepository.findAllByPrawn(currentPrawn)
        var currentVacancyRequest = VacancyApplicationRequest()

        allVacancyRequests?.forEach {
            if (it.request!!.status == RequestStatus.PENDING) {
                currentVacancyRequest = it
            }
        }

        var allVacancies = vacancyRepository.findAllByOrderBySalaryAsc() as MutableList<Vacancy>
        if (sort != null) {
            when (sort) {
                "salaryAsc" -> allVacancies = vacancyRepository.findAllByOrderBySalaryAsc() as MutableList<Vacancy>
                "salaryDesc" -> allVacancies = vacancyRepository.findAllByOrderBySalaryDesc() as MutableList<Vacancy>
                "karmaAsc" -> allVacancies = vacancyRepository.findAllByOrderByRequiredKarmaAsc() as MutableList<Vacancy>
                "karmaDesc" -> allVacancies = vacancyRepository.findAllByOrderByRequiredKarmaDesc() as MutableList<Vacancy>
                "workHoursAsc" -> allVacancies = vacancyRepository.findAllByOrderByWorkHoursPerWeekAsc() as MutableList<Vacancy>
                "workHoursDesc" -> allVacancies = vacancyRepository.findAllByOrderByWorkHoursPerWeekDesc() as MutableList<Vacancy>
            }
        }

        allVacancies.remove(currentPrawn.job)
        if (currentVacancyRequest.vacancy != null)
            allVacancies.remove(currentVacancyRequest.vacancy!!)

        model.addAttribute("user", currentPrawn)
        model.addAttribute("current_job", currentPrawn.job)
        model.addAttribute("current_application", currentVacancyRequest)
        model.addAttribute("all_vacancies", allVacancies)
        return "prawns/prawn__vacancies.html"
    }

    @PostMapping("/vacancyApplication/{id}")
    fun applyToVacancy(@PathVariable id: Long, principal: Principal, redirect: RedirectAttributes) : String {
        val currentPrawn = prawnRepository?.findByUserId(userRepository?.findByLogin(principal.name)!!.id!!)!!
        val newRequest = Request().apply { this.status = RequestStatus.PENDING }
        val allVacancyRequests = vacancyApplicationRequestRepository.findAllByPrawn(currentPrawn)

        allVacancyRequests?.forEach {
            if (it.request!!.status == RequestStatus.PENDING) {
                redirect.addFlashAttribute("error", "You cannot have more than 1 pending vacancy application request.")
                return "redirect:/prawn/vacancies"
            }
        }

        val applicationVacancy = vacancyRepository.findById(id)
        if (!vacancyRepository.findById(id).isPresent) {
            redirect.addFlashAttribute("error", "Such vacancy does not exist.")
            return "redirect:/prawn/vacancies"
        }

        val existingVacancy = applicationVacancy.get()

        when {
            existingVacancy.vacantPlaces == 0L -> {
                redirect.addFlashAttribute("error", "No vacant places left. Check back later or choose another vacancy.")
                return "redirect:/prawn/vacancies"
            }
            existingVacancy.requiredKarma > currentPrawn.karma -> {
                redirect.addFlashAttribute("error", "Requested vacancy's required karma is higher than yours.")
                return "redirect:/prawn/vacancies"
            }
        }

        requestRepository.save(newRequest)
        vacancyApplicationRequestRepository.save(
            VacancyApplicationRequest(currentPrawn, existingVacancy)
                .apply { this.request = newRequest }
        )

        redirect.addFlashAttribute("status", "Request sent. Wait for supervisor's decision.")
        return "redirect:/prawn/vacancies"
    }

    @PostMapping("/quitJob")
    fun withdrawMembership(principal: Principal, redirect: RedirectAttributes) : String {
        val currentPrawn = prawnRepository?.findByUserId(userRepository?.findByLogin(principal.name)!!.id!!)!!
        currentPrawn.job!!.vacantPlaces++
        currentPrawn.job = null
        prawnRepository?.save(currentPrawn)
        redirect.addFlashAttribute("status", "You have left your current job. You may now apply for other vacancies.")
        return "redirect:/prawn/vacancies"
    }

    @PostMapping("/withdrawApplication")
    fun withdrawApplication (principal: Principal, redirect: RedirectAttributes) : String {
        val currentPrawn = prawnRepository?.findByUserId(userRepository?.findByLogin(principal.name)!!.id!!)!!
        val allVacancyRequests = vacancyApplicationRequestRepository.findAllByPrawn(currentPrawn)

        allVacancyRequests?.forEach {
            if (it.request!!.status == RequestStatus.PENDING) {
                it.request!!.status = RequestStatus.REJECTED
                vacancyApplicationRequestRepository.save(it)
                redirect.addFlashAttribute("status", "Application withdrawn.")
                return "redirect:/prawn/vacancies"
            }
        }

        redirect.addFlashAttribute("error", "You have no active vacancy applications.")
        return "redirect:/prawn/vacancies"
    }

    enum class CartItemType { WEAPON, TRANSPORT }
    data class CartItem(val type: CartItemType, var id: Long, var quantity: Long)

    @PostMapping("/cart/modify")
    fun modifyCart(@RequestBody cartItem: CartItem, principal: Principal, redirect: RedirectAttributes): String {
        val currentUser = userRepository?.findByLogin(principal.name)!!
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
                    return "redirect:/prawn/shop"
                }
                val existingWeapon = possibleWeapon.get()
                if (existingWeapon.quantity == 0L) {
                    redirect.addFlashAttribute("error", "Out of stock. Check back later or choose another weapon.")
                    return "redirect:/prawn/shop"
                }
                val existingShoppingCartItemCheck = shoppingCartItemRepository.findByWeaponAndCart(existingWeapon, currentCreatingCart)
                if (existingShoppingCartItemCheck == null) {
                    if (cartItem.quantity <= 0L) {
                        redirect.addFlashAttribute("error", "Please enter a valid quantity for this item.")
                        return "redirect:/prawn/shop"
                    }
                    newShoppingCartItem.apply {
                        this.weapon = existingWeapon
                        this.weaponQuantity = cartItem.quantity
                    }
                } else {
                    if (cartItem.quantity <= 0L) {
                        shoppingCartItemRepository.delete(existingShoppingCartItemCheck)
                        redirect.addFlashAttribute("status", "Item removed from cart.")
                        return "redirect:/prawn/shop"
                    }
                    existingShoppingCartItemCheck.weaponQuantity = cartItem.quantity
                    newShoppingCartItem = existingShoppingCartItemCheck
                }
            }

            CartItemType.TRANSPORT -> {
                val possibleTransport = transportRepository.findById(cartItem.id)
                if (!possibleTransport.isPresent) {
                    redirect.addFlashAttribute("error", "Such transport does not exist.")
                    return "redirect:/prawn/shop"
                }
                val existingTransport = possibleTransport.get()
                if (existingTransport.quantity == 0L) {
                    redirect.addFlashAttribute("error", "Out of stock. Check back later or choose another transport.")
                    return "redirect:/prawn/shop"
                }
                val existingShoppingCartItemCheck = shoppingCartItemRepository.findByTransportAndCart(existingTransport, currentCreatingCart)
                if (existingShoppingCartItemCheck == null) {
                    if (cartItem.quantity <= 0L) {
                        redirect.addFlashAttribute("error", "Please enter a valid quantity for this item.")
                        return "redirect:/prawn/shop"
                    }
                    newShoppingCartItem.apply {
                        this.transport = existingTransport
                        this.transportQuantity = cartItem.quantity
                    }
                } else {
                    if (cartItem.quantity <= 0L) {
                        shoppingCartItemRepository.delete(existingShoppingCartItemCheck)
                        redirect.addFlashAttribute("status", "Item removed from cart.")
                        return "redirect:/prawn/shop"
                    }
                    existingShoppingCartItemCheck.transportQuantity = existingShoppingCartItemCheck.transportQuantity!! + cartItem.quantity
                    newShoppingCartItem = existingShoppingCartItemCheck
                }
            }
        }

        shoppingCartItemRepository.save(newShoppingCartItem.apply { this.cart = currentCreatingCart })

        redirect.addFlashAttribute("status", "Added to cart!")
        return "redirect:/prawn/shop"
    }

    @PostMapping("/cart/sendRequest")
    fun sendPurchaseRequest(principal: Principal, redirect: RedirectAttributes) : String {
        val newRequest = Request().apply { this.status = RequestStatus.PENDING }
        val currentUser = userRepository?.findByLogin(principal.name)!!
        val currentPrawn = prawnRepository?.findById(currentUser.id!!)!!.get()
        val currentCreatingCart = when (shoppingCartRepository.findAllByUserAndStatus(currentUser, ShoppingCartStatus.CREATING)) {
            null -> {
                redirect.addFlashAttribute("error", "You have no carts in creation.")
                return "redirect:/prawn/shop"
            }
            else -> {
                shoppingCartRepository.findAllByUserAndStatus(currentUser, ShoppingCartStatus.CREATING)!![0]
            }
        }

        if (currentCreatingCart.items == null || currentCreatingCart.items!!.size == 0) {
            redirect.addFlashAttribute("error", "You have no items in your cart.")
            return "redirect:/prawn/shop"
        }

        val cartItems = currentCreatingCart.items
        var cartSum = 0.0

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

        cartItems.forEach {
            if (it.weapon != null) {
                cartSum += it.weapon!!.price * it.weaponQuantity!!
            }
            if(it.transport != null) {
                cartSum += it.transport!!.price * it.transportQuantity!!
            }
        }
        
        if (currentPrawn.balance < cartSum) {
            redirect.addFlashAttribute("error", "Insufficient funds, please edit your cart.")
            return "redirect:/client/cart"
        }

        currentCreatingCart.status = ShoppingCartStatus.REQUESTED
        shoppingCartRepository.save(currentCreatingCart)
        purchaseRequestRepository.save(PurchaseRequest(currentUser, currentCreatingCart).apply { this.request = newRequest })

        redirect.addFlashAttribute("status", "Request sent. Await for your managing employee's decision.")
        return "redirect:/prawn/shop"
    }
}