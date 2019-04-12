package mnu

import mnu.model.DistrictHouse
import mnu.repository.DistrictHouseRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class DbSeeder(
    @Value("\${mnu.seed-db}")
    private val shouldSeed: Boolean,

    private val districtHouseRepository: DistrictHouseRepository
) {
    @PostConstruct
    fun init() {
        if (shouldSeed) {
            for (i in 0..14)
                for (j in 0..14)
                    try {
                        districtHouseRepository.save(DistrictHouse(i, j))
                    }
                    catch (e: Exception) {}
        }
    }
}