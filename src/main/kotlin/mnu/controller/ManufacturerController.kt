package mnu.controller

import mnu.form.NewEmailForm
import mnu.form.NewPasswordForm
import mnu.form.NewProductForm
import mnu.form.NewVacancyForm
import mnu.model.enums.RequestStatus
import mnu.model.enums.TransportType
import mnu.model.enums.WeaponType
import mnu.model.request.NewTransportRequest
import mnu.model.request.NewVacancyRequest
import mnu.model.request.NewWeaponRequest
import mnu.model.request.Request
import mnu.repository.request.*
import mnu.repository.shop.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
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
            newWeaponRequestRepository?.save(newProductRequest.apply { this.request = newRequest })
        if (newProductRequest is NewTransportRequest)
            newTransportRequestRepository?.save(newProductRequest.apply { this.request = newRequest })

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

        newVacancyRequestRepository?.save(newVacancyRequest)

        redirect.addFlashAttribute("form", form)
        redirect.addFlashAttribute("status", "Request submitted. Await for administrator's decision.")
        return "redirect:/manufacturer/market"
    }
}