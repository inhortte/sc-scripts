(
  s.options.numBuffers = 8192; 
  s.boot;
)
s.plotTree;

Env.perc(0.01, 1, curve: 20).plot;
(
  SynthDef(\samplePlayer, { arg out = 0, bufnum, amp = 0.3, pan = 0, dur = 1;
    var env = Env.perc(0.01, dur * 0.99, curve: 20).kr(2);
    Out.ar(out, 
      Pan2.ar(
        PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum)) * amp * env,
        pan,
        doneAction: Done.freeSelf
      );
    );
  }).add;
)

Array.fill(10, { arg n; 2 + (n * 0.2) });
Array.fill(20, { arg n; 4 - (n * 0.2) + 0.15 });

(
  var bombBuffer = Buffer.read(s, "/flavigula/xian/the-atom-bomb.wav");
  var dursLeft = Array.fill(10, { arg n; 2 + (n * 0.2) });
  var dursRight = Array.fill(10, { arg n; 2.1 + (n * 0.2) });
  var dursMidLeft = Array.fill(20, { arg n; 4 - (n * 0.2) + 0.05 });
  var dursMidRight = Array.fill(20, { arg n; 4 - (n * 0.2) + 0.15 });

  Pbind(
    \instrument, \samplePlayer,
    \dur, Pseq([2], inf),
    \amp, 0.1,
    \bufnum, bombBuffer
  ).play;
  Pbind(
    \instrument, \samplePlayer,
    \dur, Pseq(dursLeft, inf),
    \amp, 0.1,
    \bufnum, bombBuffer,
    \pan, -1
  ).play;
  Pbind(
    \instrument, \samplePlayer,
    \dur, Pseq(dursRight, inf),
    \amp, 0.1,
    \bufnum, bombBuffer,
    \pan, 1
  ).play;
  Pbind(
    \instrument, \samplePlayer,
    \dur, Pseq(dursMidLeft, inf),
    \amp, 0.1,
    \bufnum, bombBuffer,
    \pan, -0.5
  ).play;
  Pbind(
    \instrument, \samplePlayer,
    \dur, Pseq(dursMidRight, inf),
    \amp, 0.1,
    \bufnum, bombBuffer,
    \pan, 0.5
  ).play;
)
