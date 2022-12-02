package cn.bixin.sona.driver.factory

import cn.bixin.sona.component.SonaComponent
import cn.bixin.sona.component.audio.AudioComponent
import cn.bixin.sona.driver.ComponentFinder
import cn.bixin.sona.driver.ComponentProducer
import cn.bixin.sona.driver.RoomDriver

class AudioFactory : ComponentProducer.ComponentFactory {

    override fun createComponent(roomDriver: RoomDriver): SonaComponent? {
        val audio = ComponentFinder.find(AudioComponent::class.java)
        audio?.setMessageDispatcher(roomDriver)
        audio?.setProvider(roomDriver)
        return audio
    }
}