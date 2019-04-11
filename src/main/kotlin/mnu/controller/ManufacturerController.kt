package mnu.controller

import mnu.form.NewEmailForm
import mnu.form.NewPasswordForm
import mnu.form.NewProductForm
import mnu.form.NewVacancyForm
import mnu.repository.request.*
import mnu.repository.shop.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.security.Principal

@Controller
@RequestMapping("/manufacturer")
class ManufacturerController : ApplicationController() {
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

    @GetMapping("/market")
    fun market(): String {
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
}