package mnu

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class EmailSender (
    val emailSender: JavaMailSender
) {
    @PostConstruct
    fun init() {
//        val message = SimpleMailMessage()
//        message.setTo("alexk2109@gmail.com")
//        message.subject = "test"
//        message.text = "KURSACH SOSET"
//        emailSender.send(message)
    }
}