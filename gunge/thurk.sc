s.boot;
(
  Pbind(
    \freq, Pn(Pseries(110, 111, 10), 2),
    \dur, 1/2,
    \legato, Pwhite(0.1, 1)
  ).play;
    Pbind(
    \freq, Pn(Pseries(220, 222, 10), 4),
    \dur, 1/4,
    \legato, Pwhite(0.1, 1)
  ).play;
  Pbind(
    \freq, Pn(Pseries(330, 333, 10), 6),
    \dur, 1/6,
    \legato, 0.1
  ).play;
)

(
  "Call me".postln;
  "Lepton-boy".postln;
)

{ [SinOsc.ar(440, 0, 0.2), SinOsc.ar(442, 0, 0.2)] }.play;

f = { "You, die!".postln }

f.value;

{ [SinOsc.ar(440, 0, 0.2), SinOsc.ar(442, pi * 1, 0.2)] }.play;
{ Pan2.ar(PinkNoise.ar(SinOsc.kr(0.3, 1.5pi, 0.3, 0.4)), SinOsc.kr(0.2)) }.play;

(
  var aBoink, aNoise;
  aBoink = {
    var ampOsc, amp2Osc;
    ampOsc = SinOsc.kr(0.3, 1.5pi, 0.5, 0.5);
    amp2Osc = SinOsc.kr(0.3, 1.3pi, 0.5, 0.5);
    [ SinOsc.ar(440, 0, ampOsc), SinOsc.ar(443, 0, amp2Osc) ]
  };
  aNoise = { Pan2.ar(PinkNoise.ar(SinOsc.kr(0.3, 1.5pi, 0.3, 0.4)), SinOsc.kr(0.2)) };
  aBoink.play;
  aNoise.play;
)

b = { arg m; SinOsc.kr(m, 1.5pi, 0.5, 0.5); }

(
  {
    var plaintive, shift, gobble, purify;
    plaintive = PinkNoise.ar(0.3 * b.value(0.3)) + Saw.ar(440, 0.1 * b.value(0.2));
    shift = PinkNoise.ar(0.2 * b.value(0.3)) + Saw.ar(224, 0.1 * b.value(0.2));
    gobble = [ plaintive, shift ];
    purify = [ SinOsc.ar(220, 0.5pi, SinOsc.kr(16, 0.5pi, 0.1, 0.1)), SinOsc.ar(444, 1.5pi, SinOsc.kr(12, 0.5pi, 0.1, 0.1)) ];
    Mix.new([gobble, purify]).postln;
  }.plot;
)

(
  {
    var slur = 1;
    Mix.fill(slur, { arg idx;
      var left, right;
      left = SinOsc.ar(220 + idx, 0, 0.1 * (1 / idx));
      right = SinOsc.ar(220 - idx, 0.5pi, 0.1 * (1 / idx));
      [ left, right ];
    });
  }.scope;
)

(
  play({
    VarSaw.ar(
      [58.27, 58.7], 
      0, 
      SinOsc.kr(0.01,1).range(0.05, 0.95), 
      0.3
    ) ! 2 + VarSaw.ar(
      [87.31, 88.2], 
      0, 
      SinOsc.kr(0.06, 0.3).range(0.2, 0.8), 
      0.3
    ) ! 2 + 
    RLPF.ar(
      VarSaw.ar(
        [
          SinOsc.kr(0.3, 0).range(1100, 1116), // 1108.73 (Db)
          SinOsc.kr(0.3, 1).range(1653, 1669) // 1661.22 (Ab)
        ],
        0.0,
        0.2,
        0.05
      ),
      SinOsc.kr(0.08, 0.7).range(554 * 1.25, 830 * 2.2),
      0.1,
    ) ! 2 * 0.2
  });
)
