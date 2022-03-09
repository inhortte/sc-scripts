s.plotTree;
MIDIClient.destinations;
(
  var eesseq, restletsArr, restlets, leprosies, durs1, durs2;
  s.options.numBuffers = 8192; 
  s.newBusAllocators;
  s.waitForBoot({
    s.freeAll;
    Buffer.freeAll;
    s.sync;

    MIDIClient.disposeClient;
    MIDIClient.init; 
    ~midiOut = MIDIOut(1);
    // MIDIClient.destinations;
    ~midiOut.connect(2);
    ~midiOut2.connect(1);
  });

  eesseq = Pseq(([67, 63, 61, 67, 63, 61, 67, 63, 61, 67, 63, 61, 67, 63, 61, 67] - 12).midicps, 1); 
  restletsArr = Array.fill(89, { [[65, 63].wchoose([0.8, 0.2]), [62, 60].wchoose([0.6, 0.4]), [69, 68].wchoose([0.9, 0.1])]; }).flatten() - 12;
  restlets = Pseq(restletsArr.midicps, 1);

  ~argonTD = Pbind(
    \type, \midi,
    \midicmd, \noteOn,
    \midiout, ~midiOut,
    \chan, 0,
    \dur, 0.25,
    \freq, Pseq([restlets, [56, 55].midicps], 1),
    \legato, Pwhite(0.6, 0.9, inf),
    \hasGate, true
  );

  leprosies = [
    50, 52, 55, // d e g
    53, 55, 58, // f g bes
    55, 57, 61, // g a cis
    52, 54, 58, // e fis bes
    54, 56, 60, // ges aes c
    // 57, 59, 62, 61, // a b d des
    55, 56, 60, // g aes c
    57, 59, 62, // a b d
    60, 61, 66, // c des fis
    55, 57, 61, // g a cis
    61, 63, 67, // des ees g
    60, 61, 67, // c des g
    64, 66, 71, // e fis b
    67, 68, 74, // g aes d
  ];
  durs1 = [0.33333333333, 0.6666666666667, 3] * 0.5;
  durs2 = [0.33333333333, 0.6666666666667, 0.333333333333, 2.66666666666667] * 0.5;

  ~argonRadiohead = Pbind(
  \type, \midi,
    \midicmd, \noteOn,
    \midiout, ~midiOut,
    \chan, 0,
    \dur, Pseq([
      Pseq(durs1, 5),
      // Pseq(durs2, 1),
      Pseq(durs1, 8)
    ]),
    \freq, Pseq(leprosies.midicps, 1),
    \legato, Pwhite(0.6, 0.9, inf),
    \hasGate, true
  );
)

b = ~argonTD.play(TempoClock(64.0/60.0));
b.stop;

c = ~argonRadiohead.play(TempoClock(64.0/60.0));
c.stop;

Array.fill(68, { [[65, 63].wchoose([0.8, 0.2]), [62, 60].wchoose([0.6, 0.4]), [69, 68].wchoose([0.9, 0.1])]; }).flatten() - 12;
MIDIClient.destinations;
