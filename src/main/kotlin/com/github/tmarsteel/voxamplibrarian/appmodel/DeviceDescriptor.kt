package com.github.tmarsteel.voxamplibrarian.appmodel

interface DeviceDescriptor {
    val name: String
    val parameters: List<DeviceParameter>
}