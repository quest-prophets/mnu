package mnu.controller

import mnu.form.NewEmailForm
import mnu.form.NewPasswordForm
import mnu.repository.request.*
import mnu.repository.shop.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.security.Principal

@Controller
@RequestMapping("/client")
class ClientController : ApplicationController() {
    @Autowired
    val shoppingCartItemRepository: ShoppingCartItemRepository? = null
    @Autowired
    val shoppingCartRepository: ShoppingCartRepository? = null

    @Autowired
    val purchaseRequestRepository: PurchaseRequestRepository? = null
    @Autowired
    val newWeaponRequestRepository: NewWeaponRequestRepository? = null
    @Autowired
    val newTransportRequestRepository: NewTransportRequestRepository? = null
    @Autowired
    val newVacancyRequestRepository: NewVacancyRequestRepository? = null

    @GetMapping("/shop")
    fun clientsShop(model: Model, principal: Principal): String {
        val currentClient = clientRepository?.findByUserId(userRepository?.findByLogin(principal.name)!!.id!!)
        model.addAttribute("user", currentClient)
        return "customers/customer__shop.html"
    }
}