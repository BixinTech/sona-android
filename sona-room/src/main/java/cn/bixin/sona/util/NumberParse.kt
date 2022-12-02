package cn.bixin.sona.util

/**
 *
 * @Author luokun
 * @Date 2020-01-09
 */

object NumberParse {

    @JvmStatic
    fun parseInt(content: String?, default: Int): Int {
        if (content?.isNotEmpty() == true) {
            try {
                return Integer.parseInt(content)
            } catch (e: Exception) {
                return default
            }
        }
        return default
    }
}