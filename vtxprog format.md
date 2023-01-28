# VTXPROG File Format

_All numbers hex unless stated otherwise_

The file starts with this prelude:

```
0000 56 54 58 50 52 4F 47 31 30 30 30 20 00 00 00 00
0010 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
```

VTXPROG files always contain 0xB programs; ToneRoom won't
allow you to add more, and it will always save 0xB, regardless
of how many are actually customized.

Every program is 0x3E bytes long. After the prelude,
the 0xB programs are simply concatenated to the file.

## Structure of a program

Offsets are within the program, obviously.

| Offset | Length | Bit #   | Description                                                                                                                                                           |
|--------|--------|---------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 00     | 10     |         | Program name, ASCII encoded. Padded with spaces (0x20)                                                                                                                |
| 10     | 1      |         | Noise Reduction Sensitivity                                                                                                                                           |
| 11     | 1      | 0 (LSB) | ?                                                                                                                                                                     |
|        |        | 1       | Pedal 1 enabled                                                                                                                                                       |
|        |        | 2       | Pedal 2 enabled                                                                                                                                                       |
|        |        | 3       | ?                                                                                                                                                                     |
|        |        | 4       | Reverb pedal enabled                                                                                                                                                  |
|        |        | 5       | ?                                                                                                                                                                     |
|        |        | 6       | ?                                                                                                                                                                     |
|        |        | 7 (MSB) | ?                                                                                                                                                                     |
| 12     | 1      |         | Amp model (see table in the protocol documentation)                                                                                                                   |
| 13     | 1      |         | Gain value                                                                                                                                                            | 
| 14     | 1      |         | Treble value                                                                                                                                                          |
| 15     | 1      |         | Middle value                                                                                                                                                          |
| 16     | 1      |         | Bass value                                                                                                                                                            |
| 17     | 1      |         | Volume value                                                                                                                                                          |
| 18     | 1      |         | Presence Value                                                                                                                                                        |
| 19     | 1      |         | Resonance Value                                                                                                                                                       |
| 1a     | 1      |         | Bright Cap value                                                                                                                                                      |
| 1b     | 1      |         | Low Cut value                                                                                                                                                         |
| 1c     | 1      |         | Mid Boost value                                                                                                                                                       |
| 1d     | 1      |         | Bias Shift value                                                                                                                                                      |
| 1e     | 1      |         | Class value                                                                                                                                                           |
| 1f     | 1      |         | Pedal 1 Type (see table in the protocol documentation)                                                                                                                |
| 20     | 2      |         | Pedal 1 Dial 1 value, little endian byte order<br/>frequency in mHz, time in ms.<br/>_as opposed to the MIDI SysEx protocol, the value is encoded without any quirks_ |
| 22     | 1      |         | Pedal 1 Dial 2 value                                                                                                                                                  |
| 23     | 1      |         | Pedal 1 Dial 3 value                                                                                                                                                  |
| 24     | 1      |         | Pedal 1 Dial 4 value                                                                                                                                                  |
| 25     | 1      |         | Pedal 1 Dial 5 value                                                                                                                                                  |
| 26     | 1      |         | Pedal 1 Dial 6 value                                                                                                                                                  |
| 27     | 1      |         | Pedal 2 Type (see table in the protocol documentation)                                                                                                                |
| 28     | 2      |         | Pedal 2 Dial 1 value, little endian byte order<br/>frequency in mHz, time in ms.<br/>_as opposed to the MIDI SysEx protocol, the value is encoded without any quirks_ |
| 2a     | 1      |         | Pedal 2 Dial 2 value                                                                                                                                                  |
| 2b     | 1      |         | Pedal 2 Dial 3 value                                                                                                                                                  |
| 2c     | 1      |         | Pedal 2 Dial 4 value                                                                                                                                                  |
| 2d     | 1      |         | Pedal 2 Dial 5 value                                                                                                                                                  |
| 2e     | 1      |         | Pedal 2 Dial 6 value                                                                                                                                                  |
| 2f     | 8      |         | ? (likely unused, always 0 in my data)                                                                                                                                |
| 37     | 1      |         | Pedal 3 Type (see table in the protocol documentation)                                                                                                                |
| 38     | 1      |         | Pedal 3 Dial 1 value                                                                                                                                                  |
| 39     | 1      |         | Pedal 3 Dial 2 value                                                                                                                                                  |
| 3a     | 1      |         | Pedal 3 Dial 3 value (just plain milliseconds)                                                                                                                        |
| 3b     | 1      |         | Pedal 3 Dial 4 value                                                                                                                                                  |
| 3c     | 1      |         | Pedal 3 Dial 5 value                                                                                                                                                  |
| 3e     | 1      |         | ? (likely unused, always 0 in my data)                                                                                                                                |

all pedals off:  01010011
pedal 1 enabled: 


