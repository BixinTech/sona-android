package cn.bixin.sona.component.audio

class AudioStream(var streamId: String, var userId: String?, var userName: String?) {

    override fun hashCode(): Int {
        return streamId.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is AudioStream) {
            return other.streamId == streamId
        }
        return false
    }
}