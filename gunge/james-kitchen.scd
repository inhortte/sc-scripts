(
  s.options.numBuffers = 8192; 
  s.boot;
)
s.plotTree;

(
  MIDIClient.init;
  MIDIClient.destinations;
)
MIDIClient.destinations;
Array.rand(15, 5, 14) ++ Array.rand(4, 17, 20);
(
  var nts = MIDIClient.destinations[1].uid;
  var midiPattern = MIDIOut(0, nts);
  var midiCutoff = MIDIOut(0, nts);
  var dorian7 = [1, 3, 5, 7];
  var phrygian9 = [2, 4, 6, 8, 10];
  var aeolean6 = [5, 7, 9, 10];
  var locrean7 = [3, 5, 6, 8];
  var bulbous = Prand(dorian7.stutter(3) ++ phrygian9.mirror2 ++ dorian7.scramble ++ dorian7 ++ locrean7, inf);
  var patters, cutoff, resonance, pwm;
  patters = Pbind(
    \degree, bulbous,
    \dur, 0.125
    // \dur, Prand(Array.fill(128, { [0.25, 0.125, 0.0625, 0.5].wchoose([0.9, 0.1, 0.0, 0.0]) }), inf)
  );
  cutoff = Pbind(
    \control, Pbrown(20, 72, step: 3, length: inf)
  );
  resonance = Pbind(
    \control, Prand([42, 84, 56, 96], inf), 
    \dur, Prand([0.125, 0.0625, 0.25], inf)
  );
  pwm = Pbind(
    \control, Pbrown(36, 72, step: 4, length: inf)
  );
  (patters <> (type: \midi, midicmd: \noteOn, midiout: midiPattern, sustain: 0.5)).play;
  (cutoff <> (type: \midi, midicmd: \control, midiout: midiPattern, ctlNum: 43)).play;
  (resonance <> (type: \midi, midicmd: \control, midiout: midiPattern, ctlNum: 44)).play;
  (pwm <> (type: \midi, midicmd: \control, midiout: midiPattern, ctlNum: 54)).play;
)

(
  var nts = MIDIClient.destinations[1].uid;
  var midi = MIDIOut(0, nts);
  midi.allNotesOff(0);
)
