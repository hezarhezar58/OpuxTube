package dev.opux.tubeclient.core.network.newpipe

import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewPipeInitializer @Inject constructor(
    private val downloader: OkHttpDownloader,
) {
    private val initialized = AtomicBoolean(false)

    fun init(
        localization: Localization = Localization("tr", "TR"),
        country: ContentCountry = ContentCountry("TR"),
    ) {
        if (!initialized.compareAndSet(false, true)) return
        NewPipe.init(downloader, localization, country)
        Timber.i("NewPipe initialized (lang=%s, country=%s)", localization.languageCode, country.countryCode)
    }

    val youtube: StreamingService
        get() {
            if (!initialized.get()) init()
            return NewPipe.getService(ServiceList.YouTube.serviceId)
        }
}
