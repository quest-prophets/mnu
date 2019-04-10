package mnu.controller

import mnu.repository.request.*
import mnu.repository.shop.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

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
    fun manufacturerMarket(): String {
        return "manufacturers/manufacturer__market.html"
    }
}