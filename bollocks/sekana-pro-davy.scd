s.plotTree;
MIDIClient.destinations;
(
  var mainSequence, variationOne, variationTwo, variationThree, variationFour, aFirstSeq, aFirstVar, aSecondVar, aThirdVar, aForthVar, aSecondSeq;
  s.options.numBuffers = 8192; 
  s.newBusAllocators;
  s.waitForBoot({
    s.freeAll;
    Buffer.freeAll;
    s.sync;

    MIDIClient.disposeClient;
    MIDIClient.init; 
    ~midiOut = MIDIOut(0);
    ~midiOut2 = MIDIOut(1);
    // MIDIClient.destinations;
    ~midiOut.connect(2);
    ~midiOut2.connect(1);
  });

  mainSequence = Pseq([58, 61, 60, 61, 68].midicps, 1);
  variationOne = Pseq([63, 61, 60, 61].midicps, 1);
  variationTwo = Pseq([65, 64, 59, 61].midicps, 1);
  variationThree = Pseq([63, 62, 57, 61].midicps, 1);
  variationFour = Pseq([67, 64, 62, 61].midicps, 1);

  ~mullahFinaleMidi = Pbind(
    \type, \midi,
    \midicmd, \noteOn,
    \midiout, ~midiOut,
    \chan, 0,
    \dur, Pseq([0.5, 1.0, 0.5, 0.5, 0.5, 1.5, 0.5, 1.0, 1.0], inf),
    \freq, Pseq([mainSequence, variationOne, mainSequence, variationTwo, mainSequence, variationThree, mainSequence, variationFour], inf),
    \legato, Pwhite(0.6, 0.9, inf),
    \hasGate, true
  );

  aFirstSeq = Pseq([58, 61, 60, 61, 68, 63, 58, 61, 60, 61, 68, 65, 58, 61, 60, 61, 68, 63, 58, 61, 60, 61, 68, 67].midicps * 2.0, 1);
  aSecondSeq = Pseq([58, 61, 60, 61, 68].midicps * 2.0, 1);
  aFirstVar = Pseq([60, 58, 63, 65].midicps * 2.0, 1);
  aSecondVar = Pseq([60, 59, 63, 64].midicps * 2.0, 1);
  aThirdVar = Pseq([60, 58, 62, 66].midicps * 2.0, 1);
  aForthVar = Pseq([60, 59, 57, 55].midicps * 2.0, 1);

  ~argonFinaleMidi = Pbind(
    \type, \midi,
    \midicmd, \noteOn,
    \midiout, ~midiOut2,
    \chan, 0,
    \dur, Pseq([
      Pseq([0.5, 1.0, 0.5, 0.5, 0.5, 4.0], 4),
      Pseq([0.5, 1.0, 0.5, 0.5, 0.5, 1.5, 0.5, 1.0, 1.0], 4)
    ]),
    \freq, Pseq([aFirstSeq, aSecondSeq, aFirstVar, aSecondSeq, aSecondVar, aSecondSeq, aThirdVar, aSecondSeq, aForthVar], 1),
    \legato, Pwhite(0.8, 0.9, inf),
    \hasGate, true
  );
)

a = ~mullahFinaleMidi.play(TempoClock(79.5/60.0));
a.stop;
b = ~argonFinaleMidi.play(TempoClock(79.5/60.0));
b.stop;
