s.boot;
s.plotTree;

(
  ~scuttleBuf = Buffer.read(s, "/home/polaris/flavigula/xian/sheriff-film/scuttle.wav");
  SynthDef(\granulate, { | buf |
    var mg, delay1, delay2, mix;
    var winsizes = Array.fill(7, { |n| (n + 1) * 0.004 }).reverse;
    var grainrates = Array.fill(7, { |n| (n + 1).squared + 1});
    grainrates = grainrates.reverse ++ grainrates;
    winsizes = winsizes.reverse ++ winsizes;
    winsizes.postln;
    mg = MonoGrain.ar(
      in: PlayBuf.ar(1, buf, loop: 0),
      winsize: 0.007,
      grainrate: grainrates,
      winrandpct: 0,
      mul: 0.1
    );
    mix = Mix(mg);
    delay1 = DelayN.ar(mix, 0.6666, 0.6666);
    delay2 = DelayN.ar(mix, 1.33333, 1.3333);
    Out.ar(
      0, Splay.ar([mix - delay1, mix, mix - delay2]);
    );
  }).add;
)

Synth(\granulate, [buf: ~scuttleBuf]);

(
  SynthDef(\lowpong, {
    var noise = PinkNoise.ar(0.4);
    var env = EnvGen.kr(
      Env.perc,
      [0.8, 1, 0.8],
      // MouseButton.kr(0, 1, 0),
      timeScale: [0.15, 0.2, 0.13],
      doneAction: Done.freeSelf
    );
    var tension = 0.004;
    var loss = 0.8;
    var mc = MembraneCircle.ar(env * noise, tension, loss, mul: 0.7);
    Out.ar(0, Splay.ar(mc));
  }).add;
)
(
  SynthDef(\bassgrowl, {
  }).add;
)

Synth(\lowpong);

(
  var clock = TempoClock(90/60);
  var durs = [
    0.1, 0.5, 0.5, 0.5, 0.4,
    0.1, 0.3333, 0.3333, 0.33333,
    0.3333, 0.3333, 0.2333333
  ];
  Pbind(
    \instrument, \lowpong,
    \dur, Pseq(durs, 2)
  ).play(clock);
)
