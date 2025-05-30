package com.example.jamanbi.network

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "response", strict = false)
data class QualificationResponse(
    @field:Element(name = "body", required = false)
    var body: QualificationBody? = null
)

@Root(name = "body", strict = false)
data class QualificationBody(
    @field:Element(name = "items", required = false)
    var items: QualificationItems? = null
)

@Root(name = "items", strict = false)
data class QualificationItems(
    @field:ElementList(inline = true, required = false)
    var item: List<QualificationItem>? = null
)

@Root(name = "item", strict = false)
data class QualificationItem(
    @field:Element(name = "obligfldnm", required = false)
    var obligfldnm: String? = null
)
