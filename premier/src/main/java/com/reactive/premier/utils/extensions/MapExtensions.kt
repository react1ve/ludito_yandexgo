package com.reactive.premier.utils.extensions

import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.map.VisibleRegion

fun VisibleRegion.toBoundingBox() = BoundingBox(bottomLeft, topRight)
