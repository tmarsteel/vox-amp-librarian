# Alternative to VOX ToneRoom

This application aims to replace Vox ToneRoom, motivated by the fact that file import&export is broken at least since
Android 12 and they don't maintain that app anymore.  
You can use it directly in your browser: [Open Application](https://tmarsteel.github.io/vox-amp-librarian/)

<img src="https://github.com/tmarsteel/vox-amp-librarian/blob/main/doc/screenshot_desktop_mixed_schemes.png?raw=true" alt="Desktop screenshot" />
<img src="https://github.com/tmarsteel/vox-amp-librarian/blob/main/doc/screenshot_mobile_mixed_schemes.png?raw=true" alt="Mobile screenshot" width="35%" />

## Features

* Live-Edit parameters while your amp is connected
* save & load settings from your Computer / Smartphone to/from the amp
* Import settings from .VTXPROG files from ToneRoom (exporting is supported, too)
* responsive design, so you can use it on your phone or table, too
  * also supports being saved to your HomeScreen as an App (PWA)
* Very strict separation of protocol-code from application code so you can use this code to
  create your own logic / UI / automation. You could e.g. set up a raspberry to upload new configs
  to your amplifier during a show to effectively have unlimited storage capacity.

### Requirements

* USB connection to your VOX amplifier
* a browser supporting WebMIDI ([the gory details](https://caniuse.com/midi)):
  * Any recent Chrome, Edge or Firefox
* On mobile
  * a USB-OTG adapter. Google and you'll find a gazillion to buy.
  * Firefox on Android and Safari on iOS don't work :( 

## Supported Devices

* [VT20X]
* [VT40X]
* [VT100X]

Adding support for other VOX devices is likely very simple, but requires access to the hardware for reverse engineering.  
**PRs welcome!!**

See [the tips&tricks for reversing device protocols](doc/add_device.md).

## FAQs

### Connecting your device

Only one application can talk to the amp. Make sure ToneRoom is closed. Other Software that uses MIDI devices
may also interfere, try to close them, too (or try to make them ignore the amplifier device).

[VT20X]: https://voxamps.com/de/produkt/vt20x/
[VT40X]: https://voxamps.com/de/produkt/vt40x/
[VT100X]: https://voxamps.com/de/?s=VT100X

## Contributing

This app is written in Kotlin/JS and built with Gradle. Once you have Gradle, it will take care of
installing all the dependencies you need.

When adding support for a new device please make sure to also add a good description of the devices'
protocol, not just code for it.

Features needed:
* More devices! Currently everything is geared towards the VT20/40/100X. The app UI and the code
  structure would need adaptions to account for the different hardware capabilities
* Separate the protocol-code out into a Kotlin/Common source-set so others can use it to build apps that
  would run on Kotlin/JVM or Kotlin/Native.
