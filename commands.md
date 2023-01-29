# USB Protocol between ToneRoom and the amp

## About this document

_All numbers hex unless stated otherwise, e.g. as 256<sub>10</sub>._

I gathered all of this information using my own VT20X and this software:

* [Wireshark](https://www.wireshark.org/) with [USBPcap](https://desowin.org/usbpcap/)
  * you can filter for the relevant messages with `sysex.manufacturer_id == 0x42`
* [HxD](https://mh-nexus.de/de/hxd/)

# General

The Amp registers as a Midi device with manufacturer `42` (KORG Inc.) and name `Valvetronix`.
All communication is done through MIDI SysEx messages. In other words, MIDI SysEx messages are the
transport layer of this protocol.

* the `Power Level` dial is not digitally connected
* the effect knobs are not transmitted AMP->Host when the effect is not selected on the AMP
* the `VTXPROG` files used by ToneRoom use [a different format](vtxprog format.md).

# Common Values / Enums

## Program Slot/Channel Identifier

The eight Channels (Banks A + B, Channels 1-4) are identified with a single byte:

| Bank    | Channel | Number in sysex message |
|---------|---------|-------------------------|
| A/Green | 1       | `00`                    |
|         | 2       | `01`                    |
|         | 3       | `02`                    |
|         | 4       | `03`                    |
| B/Red   | 1       | `04`                    |
|         | 2       | `05`                    |
|         | 3       | `06`                    |
|         | 4       | `07`                    |

## Amp models

AMP models are identified with a single byte. The models also differ in whether they support the `BRIGHT CAP`
switch in ToneRoom and whether the second dial from the right in ToneRoom is called "TONE" or "PRESENCE".

| Model name on AMP | Model Name in ToneRoom | Actual Name                              | Number in sysex message | Brigth Cap | Precense/Tone Dial |
|-------------------|------------------------|------------------------------------------|-------------------------|------------|--------------------|
| DELUXE CL         | DELUXE CL VIBRATO      | Fender '65 Deluxe Reverb Vibrato Channel | `00`                    | YES        | PRESENCE           |
| _n/a_             | DELUXE CL NORMAL       | Fender '65 Deluxe Reverb Normal Channel  | `01`                    | NO         | PRESENCE           |
| TWEED 4x10        | TWEED 4x10 BRIGHT      | Fender Bassman 4x10 Bright Channel       | `02`                    | YES        | PRESENCE           |
| _n/a_             | TWEED 4x10 NORMAL      | Fender Bassman 4x10 Normal Channel       | `03`                    | NO         | PRESENCE           |
| _n/a_             | BOUTIQUE CL            | Overdrive Special Clean Channel          | `04`                    | YES        | PRESENCE           |
| BTQ OD            | BOUTIQUE OD            | Overdrive Special Overdrive Channel      | `05`                    | YES        | PRESENCE           |
| _n/a_             | VOX AC30               | VOX AC30                                 | `06`                    | YES        | TONE               |
| VOX AC30TB        | VOX AC30TB             | VOX AC30TB                               | `07`                    | YES        | TONE               |
| _n/a_             | BRIT 1959 TREBLE       | Marshall JTM                             | `08`                    | YES        | PRESENCE           |
| _n/a_             | BRIT 1959 NORMAL       | Marshall JTM                             | `09`                    | NO         | PRESENCE           |
| BRIT 800          | BRIT 800               | Marshal JCM800                           | `0A`                    | YES        | PRESENCE           |
| _n/a_             | BRIT VM                | Marshal JVM410                           | `0B`                    | YES        | PRESENCE           |
| _n/a_             | SL-OD                  | Soldano SLO-100                          | `0C`                    | YES        | PRESENCE           |
| DOUBLE REC        | DOUBLE REC             | Mesa Boogie Dual Rectifier               | `0D`                    | YES        | PRESENCE           |
| _n/a_             | CALI ELATION           | ?                                        | `0E`                    | YES        | PRESENCE           |
| _n/a_             | ERUPT III CH2          | EVH 5150 III Channel 2                   | `0F`                    | NO         | PRESENCE           |
| _n/a_             | ERUPT III CH3          | EVH 5150 III Channel 3                   | `10`                    | YES        | PRESENCE           |
| _n/a_             | BOUTIQUE METAL         | Diezel VH4                               | `11`                    | NO         | PRESENCE           |
| BRIT OR MKII      | BRIT OR MKII           | Orange Super Crush 100                   | `12`                    | YES        | PRESENCE           |
| _n/a_             | ORIGINAL CL            | VOX VTX                                  | `13`                    | YES        | PRESENCE           |

## Pedal Identifiers

| Slot    | Name on Amp | Name in ToneRoom | Pedal number in sysex message |
|---------|-------------|------------------|-------------------------------|
| PEDAL 1 | COMP        | COMP             | `00`                          |
|         | CHORUS      | CHORUS           | `01`                          |
|         | OVERDRIVE   | TUBE OD          | `02`                          |
|         | _n/a_       | GOLD DRIVE       | `03`                          |
|         | _n/a_       | TREBLE BOOST     | `04`                          |
|         | _n/a_       | RC TURBO         | `05`                          |
|         | DISTORTION  | ORANGE DIST      | `06`                          |
|         | _n/a_       | FAT DIST         | `07`                          |
|         | _n/a_       | BRIT LEAD        | `08`                          |
|         | _n/a_       | FUZZ             | `09`                          |
| PEDAL 2 | FLANGER     | FLANGER          | `00`                          |
|         | _n/a_       | BLK PHASER       | `01`                          |
|         | PHASER      | ORG PHASER 1     | `02`                          |
|         | _n/a_       | ORG PHASER 2     | `03`                          |
|         | TREMOLO     | TREMOLO          | `04`                          |
|         | DELAY       | TAPE ECHO        | `05`                          |
|         | _n/a_       | ANALOG DELAY     | `06`                          |
| REVERB  | ROOM        | ROOM             | `00`                          |
|         | SPRING      | SPRING           | `01`                          |
|         | HALL        | HALL             | `02`                          |
|         | PLATE       | PLATE            | `03`                          |

## Encoding for Frequency (Hz) and Time (s) values

Frequencies and time for effects are semantically mHz (millihertz) and milliseconds, respectively.

However, the encoding has a quirky artifact. After 127<sub>10</sub> steps, the encoded value jumps by 129<sub>10</sub>. So if you look at e.g. the
values sent through the protocol for the `SPEED` dial of the `CHORUS` effect:

| protocol         | reported in ToneRoom UI | Note            |
|------------------|-------------------------|-----------------|
| 120<sub>10</sub> | 0.120<sub>10</sub>Hz    |                 |
| 121<sub>10</sub> | 0.121<sub>10</sub>Hz    |                 |
| 122<sub>10</sub> | 0.122<sub>10</sub>Hz    |                 |
| 123<sub>10</sub> | 0.123<sub>10</sub>Hz    |                 |
| 124<sub>10</sub> | 0.124<sub>10</sub>Hz    |                 |
| 125<sub>10</sub> | 0.125<sub>10</sub>Hz    |                 |
| 126<sub>10</sub> | 0.126<sub>10</sub>Hz    |                 |
| 127<sub>10</sub> | 0.127<sub>10</sub>Hz    |                 |
| 256<sub>10</sub> | 0.128<sub>10</sub>Hz    | *here it jumps* |
| 257<sub>10</sub> | 0.129<sub>10</sub>Hz    |                 |
| 258<sub>10</sub> | 0.130<sub>10</sub>Hz    |                 |
| 259<sub>10</sub> | 0.131<sub>10</sub>Hz    |                 |
| 260<sub>10</sub> | 0.132<sub>10</sub>Hz    |                 |


_(and same for milliseconds for the delay times)_

You can convert from the protocol values to Hertz or seconds with this formula:

```
herzOrSeconds = (protocolValue - floor(protocolValue / 0x100) * 0x80) / 0x3E8
```

And the other way around, from Hertz or seconds to protocol values:

```
temp = herzOrSeconds * 0x3E8
protocolValue = temp + floor(temp / 0x80) * 0x80
```

## Program Format

Both host program and the amp can send a full program to each other. It always uses this format:

| Offset | Length | Description                                                                             |
|--------|--------|-----------------------------------------------------------------------------------------|
| 00     | 07     | Bytes 00-06 of the program name                                                         |
| 07     | 1      | ? (00, 8th byte)                                                                        |
| 08     | 07     | Bytes 07-0D of the program name                                                         |
| 0F     | 1      | ? (00, 8th byte)                                                                        |
| 10     | 2      | Bytes 0E-0F of the program name                                                         |
| 12     | 1      | Noise reduction sensitivity                                                             |
| 13     | 1      | ? (seems to be always 16)                                                               |
| 14     | 1      | Amp Model (see table above)                                                             |
| 15     | 1      | Gain                                                                                    |
| 16     | 1      | Treble                                                                                  |
| 17     | 1      | ? (00, 8th byte)                                                                        |
| 18     | 1      | Middle                                                                                  |
| 19     | 1      | Bass                                                                                    |
| 1A     | 1      | Volume                                                                                  |
| 1B     | 1      | Presence                                                                                |
| 1C     | 1      | Resonance                                                                               |
| 1D     | 1      | Bright Cap                                                                              |
| 1E     | 1      | Low Cut                                                                                 |
| 1F     | 1      | ? (00, 8th byte)                                                                        |
| 20     | 1      | Mid Boost                                                                               |
| 21     | 1      | Tube Bias Shift                                                                         |
| 22     | 1      | Amp Class                                                                               |
| 23     | 1      | Pedal 1 Type (see pedal identifiers)                                                    |
| 24     | 2      | Pedal 1 Dial 1, little endian byte order<br>**!! Hz value, see above for the encoding** |
| 26     | 1      | Pedal 1 Dial 2                                                                          |
| 27     | 1      | ? (sometimes 00, sometimes 20)                                                          |
| 28     | 1      | Pedal 1 Dial 3                                                                          |
| 29     | 1      | Pedal 1 Dial 4                                                                          |
| 2A     | 1      | Pedal 1 Dial 5                                                                          |
| 2B     | 1      | Pedal 1 Dial 6                                                                          |
| 2C     | 1      | Pedal 2 Type (see pedal identifiers)                                                    |
| 2D     | 2      | Pedal 2 Dial 1, little endian byte order<br>**!! Hz value, see above for the encoding** |
| 2F     | 1      | ? (00, 8th byte)                                                                        |
| 30     | 1      | Pedal 2 Dial 2                                                                          |
| 31     | 1      | Pedal 2 Dial 3                                                                          |
| 32     | 1      | Pedal 2 Dial 4                                                                          |
| 33     | 1      | Pedal 2 Dial 5                                                                          |
| 34     | 1      | Pedal 2 Dial 6                                                                          |
| 35     | A      | ? (seem to be always 0)                                                                 |
| 3E     | 1      | Pedal 3 Type (see pedal identifiers)                                                    |
| 3F     | 1      | ? (00, 8th byte)                                                                        |
| 40     | 1      | Pedal 3 Dial 1                                                                          |
| 41     | 1      | Pedal 3 Dial 2                                                                          |
| 42     | 1      | Pedal 3 Dial 3                                                                          |
| 43     | 1      | Pedal 3 Dial 4                                                                          |
| 44     | 1      | Pedal 3 Dial 5                                                                          |
| 45     | 1      | ? (probably always 0)                                                                   |

# Bidirectional Commands

These commands can be sent both by the host or the amp to communicate changes
in the settings (e.g. dial turned).  
The amp will just send these to the host. However, when the host sends these to the
amp, the amp will ACK them with `30 00 01 34 23` (unless stated otherwise for a specific command).

## Amp Dial Turned

`30 00 01 34 41 04 XX YY YY`

* `XX` is the dial identifier
* `YY` is the value, in little-endian byte order

| Dial          | number in the sysex message | Min Value | Max Value | Notes                   |
|---------------|-----------------------------|-----------|-----------|-------------------------|
| Gain          | `00`                        | `00`      | `64`      |                         |
| Treble        | `01`                        | `00`      | `64`      |                         |
| Middle        | `02`                        | `00`      | `64`      |                         |
| Bass          | `03`                        | `00`      | `64`      |                         |
| Volume        | `04`                        | `00`      | `64`      |                         |
| Presence/Tone | `05`                        | `00`      | `64`      |                         |
| Resonance     | `06`                        | `00`      | `64`      |                         |
| Bright Cap    | `07`                        | `00`      | `01`      | 00 off, 01 on           |
| Low Cut       | `08`                        | `00`      | `01`      | 00 off, 01 on           |
| Mid Boost     | `09`                        | `00`      | `01`      | 00 off, 01 on           |
| Tube Bias     | `0A`                        | `00`      | `02`      | 00 off, 01 cold, 02 hot |
| Amp Class     | `0B`                        | `00`      | `01`      | 00 A, 01 A/B            |

## Effect Dial Turned

`30 00 01 34 41 XX YY ZZ ZZ`

* `XX` is the slot identifier (see table)
* `YY` is the dial identifier, from 00 to 05; meaning varies with effect
* `ZZ ZZ` is the value, in little-endian byte order


|Slot          |Effect       |Dial Name|Number in sysex message|Range                 |Notes |
|--------------|-------------|---------|-----------------------|----------------------|------|
|PEDAL 1 (`05`)|COMP         |SENS     |`00`                   |`0000`-`0064`         |      |
|              |             |LEVEL    |`01`                   |`0000`-`0064`         |      |
|              |             |ATTACK   |`02`                   |`0000`-`0064`         |      |
|              |             |VOICE    |`03`                   |`0000`-`0002`         |      |
|              |CHORUS       |SPEED    |`00`                   |`0064`-`4e10`         |**!! Hz value, see above for the encoding**|
|              |             |DEPTH    |`01`                   |`0000`-`0064`         |      |
|              |             |MANUAL   |`02`                   |`0000`-`0064`         |      |
|              |             |MIX      |`03`                   |`0000`-`0064`         |      |
|              |             |LOW CUT  |`04`                   |`0000`/`0001` (off/on)|      |
|              |             |HIGH CUT |`05`                   |`0000`/`0001` (off/on)|      |
|              |TUBE OD,<br>GOLD DRIVE,<br>TREBLE BOOST,<br>RC TURBO,<br>ORANGE DIST,<br>FAT DIST,<br>BRIT LEAD,<br>FUZZ|DRIVE    |`00` |`00`-`64`||
|              |             |TONE     |`01`                   |`0000`-`0064`         |      |
|              |             |LEVEL    |`02`                   |`0000`-`0064`         |      |
|              |             |TREBLE   |`03`                   |`0000`-`0064`         |      |
|              |             |MIDDLE   |`04`                   |`0000`-`0064`         |      |
|              |             |BASS     |`05`                   |`0000`-`0064`         |      |
|PEDAL 2 (`06`)|FLANGER      |SPEED    |`00`                   |`0064`-`2708`         |**!! Hz value, see above for the encoding**|
|              |             |DEPTH    |`01`                   |`0000`-`0064`         |      |
|              |             |MANUAL   |`02`                   |`0000`-`0064`         |      |
|              |             |LOW CUT  |`03`                   |`0000`/`0001` (off/on)|      |
|              |             |HIGH CUT |`04`                   |`0000`/`0001` (off/on)|      |
|              |             |RESONANCE|`05`                   |`0000`-`0064`         |      |
|              |BLK PHASER<br>ORG PHASER 1<br>ORG PHASER 2|SPEED |`00` |`0064`-`4e10` |**!! Hz value, see above for the encoding**|
|              |             |RESONANCE|`01`                   |`0000`-`0064`         |      |
|              |             |MANUAL   |`03`                   |`0000`-`0064`         |      |
|              |             |DEPTH    |`04`                   |`0000`-`0064`         |      |
|              |TREMOLO      |SPEED    |`00`                   |`0c72`-`4e10`         |**!! Hz value, see above for the encoding**|
|              |             |DEPTH    |`01`                   |`0000`-`0064`         |      |
|              |             |DUTY     |`02`                   |`0000`-`0064`         |      |
|              |             |SHAPE    |`03`                   |`0000`-`0064`         |      |
|              |             |LEVEL    |`04`                   |`0000`-`0064`         |      |
|              |TAPE ECHO<br>ANALOG DELAY|TIME     |`00`       |`001e`-`0930`         |**!! ms value, see above for the encoding**|
|              |             |LEVEL    |`01`                   |`0000`-`0064`         |      |
|              |             |FEEDBACK |`02`                   |`0000`-`0064`         |      |
|              |             |TONE     |`03`                   |`0000`-`0064`         |      |
|              |             |MOD SPEED|`04`                   |`0000`-`0064`         |      |
|              |             |MOD DEPTH|`06`                   |`0000`-`0064`         |      |
|REVERB (`08`) |_all_        |MIX      |`00`                   |`0000`-`0064`         |      |
|              |             |TIME     |`01`                   |`0000`-`0064`         |      |
|              |             |PRE DELAY|`02`                   |`0000`-`0064`         |      |
|              |             |LOW DAMP |`03`                   |`0000`-`0064`         |      |
|              |             |HIGH DAMP|`04`                   |`0000`-`0064`         |      |


## User Program Changed (Bank+Channel presets)

`30 00 01 34 4e 00 XX`

Where `XX` is the program identifier. This also signals when the amp switches back from manual mode
into the program slot mode.

## Simulated Amp model changed

`30 00 01 34 41 03 00 XX 00`

Where `XX` is the amp model.

## Noise reduction sensitivity changed

`30 00 01 34 41 01 00 XX 00`

Where `XX` is the sensitivity level (from 00 left to 64 right)

### Host to Amp

The AMP will respond with the setting for the tube bias (`30 00 01 34 41 04 0b XX 00`).

### Amp to Host

When turning the model Knob on the Amp, it will send this message. It will also
send set-dial messages for all dials from 00 to 0b.

## Pedal in slot enabled/disabled

`30 00 01 34 41 02 XX YY 00`

* `XX` is the slot (see below)
* `YY` is the status: `00` = disabled, `01` = enabled

| Slot    | `XX` in sysex message |
|---------|-----------------------|
| PEDAL 1 | `01`                  |
| PEDAL 2 | `02`                  |
| REVERB  | `04`                  |

# Host to AMP

## Request Currently Selected User Program Slot

`30 00 01 34 12`

The amp responds with `30 00 01 34 42 00 XX`,
where `XX` is the program identifier.

## Request User Program

`30 00 01 34 1C 00 XX`

Where `XX` is the program identifier

Amp responds with **one** message starting with `30 00 01 34 4c 00 XX 00`,
where `XX` is the program identifier (identical as requested).

The rest of the message is the program; the structure is identical to that
of "Write User Program".

## Request the currently selected program

Sent by ToneRoom after it has received all user programs

`30 00 01 34 10`

Amp responds with **one** message. starting with `30 00 01 34 40 00``. The rest of 
the message is the current program (same format as "Write User Program")

The data reported by this command can be inaccurate: the tube bias and amp class report the value it had
**before** it was last changed. Saving the program to the slot **with the AMP buttons** fixes this.

## Request User Amp presets

`30 00 01 34 31 00 XX`

Where `XX` is the preset identifier

| Preset | Number in the sysex message |
|--------|-----------------------------|
| 00     | User A                      |
| 01     | User B                      |
| 02     | User C                      |
| 03     | ??? (used by ToneRoom)      |

The amp responds with `30 00 01 34 65 00`, and in the same message,
with the data for the amp preset. This format hasn't been documented yet.

## Set Effect Pedal Type

`30 00 01 34 41 03 XX YY 00`

* `XX` is the slot (see below)
* `YY` is the pedal  (see table "Pedal Identifiers").

| Slot    | `XX` in sysex message |
|---------|-----------------------|
| PEDAL 1 | `01`                  |
| PEDAL 2 | `02`                  |
| REVERB  | `04`                  |

The amp responds with an effect dial value (see "Effect Dial Turned")

## Write User Program

`30 00 01 34 4c 00 XX 00 ...`

Where `XX` is the preset identifier. This is followed by the program.

ToneRoom follows this up with another message: `30 00 01 34 4e 00 XX`,
where `XX` is the program identifier. !! is this for saving/persisting?

# AMP to Host

## Simulated Amp model changed

`30 00 01 34 41 04 01 34 00`

## Effect Pedal Type Changed

The amp will not send a dedicated message to indicate this. Rather,
it sends one or more effect dial values (see "Effect Dial Turned").

## Switched [to] builtin presets

`30 00 01 34 4e 01 XX`

Where `XX` is the preset identifier. There currently is no obvious logical relation between
the number and the preset.

## Switch to manual mode

The amp sends `30 00 01 34 4e 02 00`.

ToneRoom reacts to this by sending a "Request the currently selected program"
message. The amp, in turn, responds with a program called `MANUAL          `.

## Manual Mode; Amp model switched

1. the amp will send a "Simulated Amp model changed" message
2. the amp will send a "Noise reduction sensitivity changed" message