package cn.bixin.sona.report

/**
 * 上报事件
 *
 * @Author luokun
 * @Date 2020/6/6
 */
class SonaReportEvent private constructor(
    val type: Int,
    val code: Int,
    val sdkCode: Int,
    val content: String,
    val reason: String
) {

    internal var productCode: String = ""
    internal var roomId: String = ""
    internal var uid: String = ""
    internal var ext: HashMap<String?, String?>? = null
    internal var snProductCode: String = ""
    internal var supplier: String = ""

    companion object Type {
        const val LOG = 1 // Log
        const val LOGAN = LOG shl 1 // Logan
    }

    class Builder {
        var code: Int = 0
            private set
        var sdkCode: Int = 0
            private set
        var content: String? = ""
            private set
        var reason: String? = ""
            private set
        var ext: HashMap<String?, String?>? = null
            private set
        var supplier: String? = ""
            private set
        var type = LOG
            private set

        fun setCode(code: Int): Builder {
            this.code = code
            return this
        }

        fun setSdkCode(sdkCode: Int): Builder {
            this.sdkCode = sdkCode
            return this
        }

        fun setContent(content: String): Builder {
            this.content = content
            return this
        }

        fun setReason(reason: String): Builder {
            this.reason = reason
            return this
        }

        fun setType(type: Int): Builder {
            this.type = type
            return this
        }

        fun setExt(ext: HashMap<String?, String?>?): Builder {
            this.ext = ext
            return this
        }

        fun setSupplier(supplier: String): Builder {
            this.supplier = supplier
            return this
        }

        fun build(): SonaReportEvent {
            val event = SonaReportEvent(type, code, sdkCode, content ?: "", reason ?: "")
            event.ext = this.ext
            event.supplier = this.supplier ?: ""
            return event
        }
    }
}