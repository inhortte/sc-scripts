(
  s.options.numBuffers = 8192; 
  s.boot;
)
s.plotTree;

(
  SynthDef(\sawFilter, {
    arg inBus = 7, outBus = 0, rate = 2.0, freq = 440, iphase = 0.0;
    var saw = LFSaw.kr(freq: rate, iphase: iphase, mul: 0.3, add: 0.7);
    var lpf = LPF.ar(
      in: In.ar(inBus, 2),
      freq: saw * freq
    );
    Out.ar(
      outBus,
      lpf
    );
  }).add;
)
{ LFSaw.kr(freq: 3, iphase: 0, mul: 0.5, add: 0.5) }.scope;
(
  SynthDef(\trianglish, { | outBus = 0, freq = 147, dur = 1, amp = 0.1 |
    var env = Env.perc(
      attackTime: 0.01,
      releaseTime: dur * 0.2,
      level: 1,
      curve: -4
    ).kr(2);
    var tri = LFTri.ar(
      freq: freq,
      iphase: [0, 1.3, 2.9, 3.2],
      mul: amp
    ) * env;
    var lpf = HPF.ar(
      in: tri,
      freq: Line.kr(freq * 7, freq / 2, dur * 0.8),
    );
    Out.ar(
      outBus,
      Splay.ar(tri)
    );
  }).add;
)
(
  SynthDef(\sawish, {
    arg outBus = 0, freq = 220, width = 0.3, amp = 0.1, dur = 1;
    var env = Env.perc(0.05, dur * 0.8, curve: -3.0).kr(2);
    var saw = VarSaw.ar(
      freq: freq,
      iphase: [ pi / 8, pi / 6, 2 * pi / 7, 2 * pi / 3 ],
      width: Line.kr(width, 0.7, dur * 0.4),
      mul: amp
    ) * env;
    Out.ar(outBus, Splay.ar(saw));
  }).add;
)
Synth(\sawish, [ freq: 220, dur: 0.5, amp: 0.5, width: 0.01 ]);
(
  SynthDef(\tremolo, {
    arg outBus = 0, inBus, rate;
    var trem = SinOsc.kr(rate, pi / 2, mul: 0.5, add: 0.5);
    var lpf = HPF.ar(
      in: In.ar(inBus, 2),
      freq: 36,
      mul: trem
    );
    Out.ar(
      outBus,
      lpf
    );
  }).add;
  SynthDef(\inOut, {
    arg outBus = 0, inBus;
    Out.ar(
      outBus,
      In.ar(inBus, 2)
    )
  }).add;
  SynthDef(\mix2Out, {
    arg outBus = 0, dBuf = 0, dt = 0.5, inBus1, inBus2;
    var input = Mix([ In.ar(inBus1, 2), In.ar(inBus2, 2) ]);
    var delay = DelayN.ar(
      in: input * 0.7, 
      maxdelaytime: 1.0,
      delaytime: dt,
      mul: 0.5,
      add: input
    );
    /*
    var delay = MultiTap.ar(
      timesArray: `[0.2, 0.4, 0.6, 1.1],
      levelsArray: `[0.5, 0.5, 0.5, 0.5],
      in: Mix([ In.ar(inBus1, 2), In.ar(inBus2, 2) ]),
      bufnum: dBuf
    );
    */
    Out.ar(outBus, delay);
  }).add;
)

(
  g = Group.basicNew(s, 1);
  d = Bus.audio(s, 2);
  Synth.head(g, \trianglish, [ outBus: d, freq: 220, dur: 20, amp: 0.7 ]);
  Synth.tail(g, \sawFilter, [ inBus: d, outBus: 0, rate: 4, freq: 880 ]);
)

(
  g = Group.basicNew(s, 1);
  c = Bus.audio(s, 2);
  d = Synth.head(g, \trianglish, [ outBus: c, freq: 220, dur: 30, amp: 0.5 ]);
  e = Synth.tail(g, \tremolo, [ outBus: 0, inBus: c, rate: 4 ]);
  // e = Synth.tail(g, \inOut, [ outBus: 0, inBus: c ]);
)
c.set(0.8);
d.map(\amp, c.index);
f = Bus.control(s, 1);
f.set(6);
e.map(\rate, f.index);

(
  var dBuf = Buffer.alloc(s, s.sampleRate);
  var ampSeq = [0.5, 0.3, 0.5, 0.3, 0.5, 0.3, 0.3, 0.3, 0.3, 0.5, 0.3, 0.3, 0.3, 0.3, 0.5, 0.3, 0.5, 0.3];
  var melodySeq1 = [0, 5, 4, 0, 5, 4, 0, 5, 4, 0, 5, 4, 0, 5, 4, 0, 5, 4];
  var melodySeq2 = [0, 5, 4, 0, 7, 5, 0, 5, 4, 0, 7, 5, 0, 5, 4, 0, 7, 5];
  var melodySeq3 = [4, 7, 5, 4, 7, 5, 4, 7, 5, 4, 7, 5, 4, 7, 5, 4, 7, 5];
  var melodySeq4 = [4, 7, 5, 4, 9, 7, 4, 10, 9, 4, 9, 7, 2, 7, 5, 0, 5, 4];
  var melodyPartOne = melodySeq1 ++ melodySeq1 ++ melodySeq1 ++ melodySeq1 ++
    melodySeq2 ++ melodySeq2 ++ melodySeq2 ++ melodySeq2 ++
    melodySeq3 ++ melodySeq3 ++ melodySeq3 ++ melodySeq3 ++
    melodySeq4 ++ melodySeq4 ++ melodySeq4 ++ melodySeq4 ++
    melodySeq2 ++ melodySeq2 ++ melodySeq2 ++ melodySeq2;
  var evilMelodyEnd = [7, 9, 7, 9, 7, 9, 7, 9, 7, 9, 7, 9, 7, 9, 12, 10, 10, 9] ++ // Eb lydian
    [7, 8, 7, 8, 7, 8, 7, 8, 7, 8, 7, 8, 7, 8, 12, 10, 10, 8] ++ // Eb ionian
    [7, 8, 7, 8, 7, 8, 7, 8, 7, 8, 7, 8, 7, 8, 12, 10, 10, 8] ++ // Eb mixolydian
    [6, 8, 6, 8, 6, 8, 6, 8, 6, 8, 6, 8, 6, 8, 12, 10, 10, 8];
  var clock = TempoClock(90/60);
  var tBus = Bus.audio(s, 2);
  var tfBus = Bus.audio(s, 2);
  var sBus = Bus.audio(s, 2);
  var thurk;
  g = Group.basicNew(s, 1);
  
  Pbind(
    \instrument, \trianglish,
    \group, g,
    \addAction, 0,
    \outBus, tBus,
    \amp, Pseq(ampSeq, 20),
    \scale, Scale.mixolydian,
    \degree, Pseq(evilMelodyEnd, 1),
    \dur, 0.5
  ).play(clock);
  // rate: 12 is 8 cycles per 90bpm beat
  Synth.tail(g, \sawFilter, [ outBus: tfBus, inBus: tBus, rate: 24, freq: 256 ]); 
  thurk = Synth.tail(g, \mix2Out, [ inBus1: tfBus, inBus2: sBus, dBuf: dBuf ]);
  Pbind(
    \instrument, \sawish,
    \group, thurk,
    \addAction, 2,
    \outBus, sBus,
    \width, 0.01,
    \amp, Pseq(ampSeq * 0.7, 20),
    \scale, Scale.mixolydian,
    \degree, Pseq(evilMelodyEnd, 1),
    \dur, 0.5
  ).play(clock);
)
{ BlitB3Saw.ar(XLine.kr(1000, 20, 10)) }.play;
(
  SynthDef(\gumish, {
    arg outBus = 0, freq = 220, width = 0.3, amp = 0.1, dur = 1;
    var env = Env.perc(0.05, dur * 1.2, curve: -3.0).kr(2);
    var bSaw = BlitB3Saw.ar(
      freq: [ freq * 1.01, freq, freq * 0.99, freq * 1.015 ],
      leak: Line.kr(0.90, 0.70, dur),
      mul: amp
    ) * env;
    var moogFF = MoogFF.ar(
      in: bSaw,
      freq: env * freq * 8,
      gain: Line.kr(3.4, 1, dur * 0.8)
    );
    var saw = VarSaw.ar(
      freq: freq,
      iphase: [ pi / 8, pi / 6, 2 * pi / 7, 2 * pi / 3 ],
      width: Line.kr(width, 1, dur * 0.4),
      mul: amp
    ) * env;
    var lpf = BPF.ar(
      in: bSaw,
      freq: XLine.kr(freq * 8, freq * 4, dur * 0.1),
      rq: 2
    );
    // Out.ar(outBus, Splay.ar(moogFF));
    // Out.ar(outBus, Splay.ar(lpf));
    Out.ar(outBus, Splay.ar(Mix([ moogFF, lpf ])));
  }).add;
  SynthDef(\growl, {
    arg outBus = 0, amp = 0.1, dur = 1;
    var env = Env.perc(0.01, dur * 0.2, curve: -5).kr(2) ! 2;
    var bn = BrownNoise.ar(amp) * env; 
    var hpf = HPF.ar(
      in: bn,
      freq: SinOsc.kr(12, pi / 4, mul: 256) + 512
    );
    Out.ar(outBus, hpf);
  }).add;
  SynthDef(\gummyFilter, {
    arg inBus = 7, outBus = 0, rate = 2.0, freq = 440, iphase = 0.0;
    var saw = LFSaw.kr(freq: rate, iphase: iphase, mul: 0.3, add: 0.7);
    var lpf = LPF.ar(
      in: In.ar(inBus, 2),
      freq: saw * freq
    );
    Out.ar(
      outBus,
      lpf
    );
  }).add;
  SynthDef(\mix3Out, {
    arg outBus = 0, dBuf = 0, dt = 0.5, inBus1, inBus2, inBus3;
    var input = Mix([ In.ar(inBus1, 2), In.ar(inBus2, 2), In.ar(inBus3, 2) ]);
    var delay = DelayN.ar(
      in: input * 0.7, 
      maxdelaytime: 1.0,
      delaytime: dt,
      mul: 0.5,
      add: input
    );
    Out.ar(outBus, delay);
  }).add;
)
Synth(\growl, [ dur: 9, amp: 0.7 ]);
Synth(\gumish, [ dur: 5, freq: 220, amp: 0.7 ]);
(
  var dBuf = Buffer.alloc(s, s.sampleRate);
  var voice1Seq = [ 7, 7, 7, 7, 7, 2, 3, 2, 3, 2, 3, -2 ] ++ [ 7, 6, 7, 7, 7, 2, 3, 2, 3, 2, 3, -2 ] ++
    [ 7, 6, 6, 7, 7, 2, 3, 2, 3, 2, 3, -2 ] ++ [ 7, 6, 6, 6, 7, 2, 3, 2, 3, 2, 3, -2 ] ++
    [ 6, 6, 6, 6, 7, 2, 3, 2, 3, 2, 3, -2 ] ++ [ 6, 6, 6, 6, 6, 2, 3, 2, 3, 2, 3, -2 ] ++ [ 6, 5, 5, 5, 6, 2, 3 ];
  var voice2Seq = [ 2, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0 ];
  var durs = [ 0.33333, 0.333333, 0.333333, 0.5, 1.5, 0.5, 1.5, 0.5, 1.5, 0.5, 0.5, 2 ];
  var voice1amps = [ 0.5, 0.3, 0.3, 0.5, 0.3, 0.5, 0.3, 0.5, 0.3, 0.5, 0.3, 0.3 ];
  var voice2amps = [ 0.5, 0.3, 0.3, 0.5, 0.3, 0, 0, 0, 0, 0, 0, 0 ];
  var growlAmps = [ 0.5, 0.3, 0.3, 0.5, 0.3, 0.5, 0.3, 0.5, 0.3, 0.5, 0.3, 0.3 ] * 0.7;
  var clock = TempoClock(90/60);
  var saw1Bus = Bus.audio(s, 2);
  var saw2Bus = Bus.audio(s, 2);
  var growlBus = Bus.audio(s, 2);
  g = Group.basicNew(s, 1);

  Pbind(
    \instrument, \gumish,
    \group, g,
    \addAction, 0,
    \outBus, saw1Bus,
    \amp, Pseq(voice1amps, inf),
    \scale, Scale.mixolydian,
    \width, 0.2,
    \degree, Pseq(voice1Seq, 1),
    \dur, Pseq(durs, inf)
  ).play(clock);
  /*
  Pbind(
    \instrument, \gumish,
    \group, g,
    \addAction, 0,
    \outBus, saw2Bus,
    \amp, Pseq(voice2amps, inf),
    \scale, Scale.mixolydian,
    \width, 0.2,
    \degree, Pseq(voice2Seq, inf),
    \dur, Pseq(durs, inf)
  ).play(clock);
  */
  Pbind(
    \instrument, \growl,
    \group, g,
    \addAction, 0,
    \outBus, growlBus,
    \amp, Pseq(growlAmps, inf),
    \dur, Pseq(durs, inf)
  ).play(clock);
  Synth.tail(g, \mix3Out, [ inBus1: saw1Bus, inBus2: saw2Bus, inBus3: growlBus, dBuf: dBuf, dt: 1.33333 ]);
)

Scale.directory;
(
  b = Scale.mixolydian;
)

// Tests

(
  SynthDef(\impulses, {
    Out.ar(
      0,
      Impulse.ar([ 0.9, 1.19 ], 0, 0.3)
    );
  }).add;
  SynthDef(\reverb, {
    Out.ar(
      0,
      CombN.ar(
        in: In.ar(0, 2),
        maxdelaytime: 0.1,
        delaytime: 0.1,
        decaytime: 4
      )
    )
  }).add;
)
(
  Synth(\reverb);
  Synth(\impulses);
)
s.queryAllNodes;
r = RootNode.new;
{ SinOsc.ar(220, 0, 0.1) }.play;
{ SinOsc.ar(330, 0, 0.1) }.play;

(
  SynthDef(\impulse, {
    Out.ar(
      0,
      Pan2.ar(
        Saw.ar(220, Decay2.ar(Impulse.ar(1), 0.01, 0.1, 0.5)),
        0.0
      )
    )
  }).add;
  SynthDef(\continuous, {
    Out.ar(0, Pan2.ar(WhiteNoise.ar(0.1), 0.0));
  }).add;
  SynthDef(\exDelay, {
    arg delayTime = 0.1;
    var input = In.ar(0, 2);
    var effect = DelayN.ar(
      in: input,
      maxdelaytime: 1,
      delaytime: delayTime
    );
    Out.ar(0, effect);
  }).add;
)

a = Group.basicNew(s, 1);
x = Synth.head(a, \impulse);
s.scope;

x.free;
x = Synth.head(a, \impulse);
y = Synth.tail(a, \exDelay, [ delayTime: 0.4 ]);

y.free;
