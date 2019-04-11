package mnu.model.request

import mnu.model.Client
import javax.persistence.*
import javax.validation.constraints.Min

@Entity
@Table (name = "new_vacancy_requests")
data class NewVacancyRequest (@Column(nullable = false) var title: String = "",
                              @Min(1) var salary: Long = 0,
                              @Min(0) var requiredKarma: Long = 0,
                              @Min(0) var workHoursPerWeek: Int = 0,
                              @Min(1) var vacantPlaces: Long = 1,

                              @ManyToOne(fetch = FetchType.EAGER)
                              @JoinColumn(name = "requester_id", referencedColumnName = "user_id")
                              var client: Client? = null) {
    @Id
    var id: Long? = null

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    var request: Request? = null
}