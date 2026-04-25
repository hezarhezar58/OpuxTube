package dev.opux.tubeclient.core.data.paging

import org.schabi.newpipe.extractor.Page
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PageCache @Inject constructor() {

    private val cache = ConcurrentHashMap<String, Page>()

    fun store(page: Page?): String? {
        if (page == null) return null
        val token = UUID.randomUUID().toString()
        cache[token] = page
        if (cache.size > MAX_ENTRIES) {
            val oldest = cache.keys.iterator()
            repeat(cache.size - MAX_ENTRIES) {
                if (oldest.hasNext()) {
                    cache.remove(oldest.next())
                }
            }
        }
        return token
    }

    fun retrieve(token: String?): Page? = token?.let { cache[it] }

    fun consume(token: String?): Page? = token?.let { cache.remove(it) }

    companion object {
        private const val MAX_ENTRIES = 64
    }
}
