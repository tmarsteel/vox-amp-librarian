# Reversing Device Protocols

The VOX devices abuse the MIDI protocl and its System-Exclusive Messages as a transport layer for a custom
protocol. You can find more details in the [protocol documentation for the VT20/40/100X device][VT20X doc].

## Supporting Software

* [Wireshark](https://www.wireshark.org/) with [USBPcap](https://desowin.org/usbpcap/)
    * you can filter for the relevant messages with `sysex.manufacturer_id == 0x42`
* [HxD](https://mh-nexus.de/de/hxd/)

## Workflow

For Host-to-Device communication, i recommend:

* connect your AMP to your PC
* open Wireshark and ToneRoom
* manipulate a parameter in ToneRoom and look at the messages in Wireshark
* you should be able to easily find the patterns. It should be very similar between all
  the different devices, so you can look at [existing protocol documentation][VT20X doc] for more info.

For Device-to-Host communication the ToneRoom+WireShark approach works, too. But IMO it sucks because
you have to click a bunch for every message in WireShark. Alternatively:

* use this application instead of ToneRoom
* set the log-level in the sidebar to "DEBUG". The application will now log all incoming messages to
  the browser console.
* Now turn knobs on your device and look at the incoming message. Figuring out the protocol should be straightforward then.

[VT20X doc]: protocols/vt_20_40_100_x.md