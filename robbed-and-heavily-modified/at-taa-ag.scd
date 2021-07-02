(
  s.options.numBuffers = 8192; 
  s.boot;
)
s.plotTree;
s.meter;
s.scope;

(
  var smpPath = PathName("/home/polaris/rummaging_round/sc-scripts/robbed-and-heavily-modified/at-taa-ag-samples");

  ~smpBuffers.do(_.free); 
  ~smpBuffers = Dictionary();
  smpPath.filesDo({ 
    arg smpfile, i;
    postln("   " + i + smpfile.fileName );
    // add a sample into a buffer, store object to Dictionary
    ~smpBuffers.add(smpfile.fileName -> Buffer.readChannel(s,
        smpfile.fullPath, channels:[0]));
  });

  // function to partially match filename for buffers
  ~getSmp = { |regexp|
    ~smpBuffers.detect { |buf|
      regexp.matchRegexp(buf.path)
    }
  };
)

(
  x = SynthDef(\help_Buffer, { arg out = 0, bufnum;
      Out.ar( out,
          PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum))
      )
  }).play(s,[\bufnum, ~getSmp.("zel-f")]);
)
x.free;

(
  var sr = ~getSmp.("zel-f").sampleRate;
  var nf = ~getSmp.("zel-f").numFrames;
  (nf / sr).postln
)

(
  SynthDef(\ggrain, {
    arg outBus, bufnum = 0, pan = 0.0, startPos = 0.0, attack = 0.01, amp = 0.1, dur = 0.2, rate = 1.0;
    //var grain = PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum), 1, BufFrames.kr(bufnum) * startPos, 0) *
    var grain = PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum) * rate, 1, startPos, 0) *
      (
        EnvGen.kr(
          Env.perc(attack, dur),
          doneAction: Done.freeSelf
        ) - 0.001
      ) * amp;
    Out.ar(outBus, Pan2.ar(grain, pan))
  }).add; 
)

Synth(\ggrain, [outBus: 0, bufnum: ~getSmp.("zel-f"), startPos: 0.0, dur: 0.5, rate: 1.0]);

(
  SynthDef(\grainy, {
    arg regexp, outBus, parts, dur;
    var smp = ~getSmp.(regexp);
    var length = smp.numFrames / smp.sampleRate;
    var durHigh = length / parts;
    var durLow = length / parts * 0.3;
    var startPoses = Array.fill(parts, {
      arg i;
      ((smp.numFrames / parts) * i).trunc;
    }).scramble;
    parts.do({
      startPoses.do({
        arg startPos;
        var dur = rrand(durLow, durHigh);
        var attack = rrand(0.01, 0.1);
        Synth(\ggrain,
          [
            \outBus, outBus,
            \bufnum, smp,
            \rate, [1.0, 0.5, 2].wchoose([0.7, 0.1, 0.2]),
            \startPos, startPos,
            \amp, 0.2, // [0.5, 0.7, 0.8, 0.3, 0.0].wchoose([0.2, 0.1, 0.05, 0.2, 0.45]) * 2,
            \pan, 1.0.rand2,
            \attack, attack,
            \dur, dur
          ]
        );
        // gap.wait;
        // (gap - dur).wait;
      });
    });
  }).add;
)

(
  ~belch = { |regexp, outBus, parts, dur, bpm|
    var multiplyer = 60 / bpm;
    var realDur = dur * multiplyer;
    var smp = ~getSmp.(regexp);
    var length = smp.numFrames / smp.sampleRate;
    var durHigh = realDur / parts;
    var durLow = realDur / parts * 0.7;
    var startPoses = Array.fill(parts, {
      arg i;
      ((smp.numFrames / parts) * i).trunc;
    }).scramble;
    Routine({
      startPoses.do({
        arg startPos;
        var dur = rrand(durLow, durHigh);
        var gap = durHigh;
        var attack = rrand(0.01, 0.02);
        Synth(\ggrain,
          [
            \outBus, outBus,
            \bufnum, smp,
            \rate, [1.0, 0.5, 2].wchoose([0.7, 0.2, 0.1]),
            \startPos, startPos,
            \amp, 0.8, // [0.5, 0.7, 0.8, 0.3, 0.0].wchoose([0.2, 0.1, 0.05, 0.2, 0.45]) * 2,
            \pan, 1.0.rand2,
            \attack, attack,
            \dur, dur
          ]
        );
        gap.wait;
        // (gap - dur).wait;
      });
    }).play
  };
)

(
  var melody = [
    ["ny-f-high", 0.5], ["fi-eb-low", 2], ["nymph-Eb-low", 0.5],
    ["orin-d-low", 0.5], ["nig-f-low", 1], ["fetz-a-low", 0.5], ["iis-f-low", 0.25], ["či-b-low", 0.25], ["mej-c", 6.5],
    ["ooowww-f-high", 0.5], ["nymph-alt-low-eb", 2], ["nymph-Eb-low", 0.5],
    ["orin-d-low", 0.5], ["tea-f-low", 1], ["no-a-low", 0.5], ["zel-f-low", 0.25], ["ka-b", 0.25], ["mej-c", 6.5],
    ["ny-f-high", 0.5], ["nymph-alt-low-eb", 2], ["fi-eb-low", 0.5],
    ["orin-d-low", 0.5], ["nig-f-low", 1], ["no-a-low", 0.5], ["křen-F", 0.25], ["či-b-low", 0.25], ["mej-c", 6.5],
    ["interesak-g-high", 0.5], ["havoc-f-low", 2], ["křen-alt-low-f", 0.5],
    ["fi-eb-low", 0.5], ["fetz-g-low", 1], ["ka-b", 0.5], ["han-e-low", 0.25], ["snowy", 0.25], ["low-d-high", 6.5],
    ["div-g-high", 0.5], ["havoc-f-low", 2], ["křen-alt-low-f", 0.5],
    ["fi-eb-low", 0.5], ["fetz-g-low", 1], ["či-b-low", 0.5], ["han-e-low", 0.25], ["snowy", 0.25], ["low-d-high", 6.5],
    ["div-g-high", 0.5], ["havoc-f-low", 2], ["křen-alt-low-f", 0.5],
    ["fi-eb-low", 0.5], ["fetz-g-low", 1], ["ka-b", 0.5], ["han-e-low", 0.25], ["snowy", 0.25], ["low-d-high", 6.5],
    ["fetz-a-low", 0.5], ["nig-f-low", 1], ["no-a-low", 0.5], ["mej-c", 0.5], ["křen-F", 0.5], 
    ["nymph-Eb-high", 0.5], ["angulo-d-low", 1], ["nymph-alt-low-e", 0.5], ["nymph-alt-low-f#", 0.05], ["fetz-a-low", 0.5], ["div-g-high", 6.5],
    ["ka-Bb", 0.5], ["no-g-low", 1], ["ka-Bb", 0.5], ["orin-d-low", 0.5], ["no-g-low", 0.5],
    ["ooowww-f-high", 0.5], ["nymph-alt-low-e", 1], ["snowy", 0.5], ["div-g-high", 0.25], ["ka-b", 0.25], ["sheep-a-low", 6.5]
  ];
  var melody2 = [
    ["dům-c", 1], ["ka-b", 0.25], ["mej-c", 0.25], ["či-b", 1.166666666666], ["angulo-d", 0.333333333],
    ["no-g-low", 0.333333333], ["nymph-Eb-low", 0.33333333], ["ooowww-f-high", 0.3333333], ["nymph-alt-low-e", 2],
    ["dům-c", 0.5], ["češač-b", 0.5], ["mej-c", 0.125], ["či-b", 0.375], ["angulo-d", 0.5], ["nymph-Eb-high", 0.125], ["angulo-d", 0.375], ["div-g-high", 0.5],
    ["no-a-low", 0.125], ["no-g-low", 0.375], ["low-d-high", 0.25], ["dům-c", 0.25], ["snowy", 0.25], ["orin-d-low", 0.25], ["han-e-low", 1.5]
  ];
  var melody3 = [
    ["ny-f-high", 0.5], ["fi-eb-low", 2], ["nymph-Eb-low", 0.5],
    ["orin-d-low", 0.5], ["nig-f-low", 1], ["fetz-a-low", 0.5], ["iis-f-low", 0.25], ["či-b-low", 0.25], ["mej-c", 6.5],
    ["ooowww-f-high", 0.5], ["nymph-alt-low-eb", 2], ["nymph-Eb-low", 0.5],
  ];
  Routine({
    melody3.do({ |note|
      ~belch.(note[0], 0, [1, 2, 3, 4, 6, 8].wchoose([0.2, 0.4, 0.1, 0.2, 0.05, 0.05]), note[1], 72);
      (note[1] * 60 / 72).wait;
    })
  }).play;
)

(
  SynthDef(\verbDelayFX, {
    arg inBus, out=0, revWet=0.5, dlyWet=0.5, feedback = 0.2 ;
    
    var snd = In.ar(inBus, 2);
    var verb = JPverb.ar(snd);
    var delay = Greyhole.ar(
      in: snd, 
      delayTime: 1.071429,
      feedback: feedback
    );
    
    snd = snd + (verb * revWet) + (delay * dlyWet) * 0.5;
    Out.ar(out, snd);
  }).add;
);


// -----------------------------------------------------------------
(
  ~reverBus = Bus.audio(s,2);
  ~reverbDelay = Synth(\verbDelayFX, [\inBus, ~reverBus], addAction: \addAfter);
)

(
  ~belch.("krzys", 0, rrand(3, 5), [0.625].choose, 10);
  // ~belch.("Db", ~reverBus, rrand(3, 6), [0.714286, 0.357143, 0.5357142].choose, 12);
)

(
  var bufs = ~bufNames.scramble.keep(6);
  bufs.postln;
  Routine({
    bufs.do({
      arg bufName;
      // var gap = rrand(1.5, 3);
      // var gap = rrand(0.8, 1.5);
      var gap = rrand(0.2, 1);
      //var gap = rrand(0.05, 0.5);
      // var parts = rrand(3, 6);
      var parts = rrand(8, 12);
      ~belch.(bufName, ~reverBus, parts, gap);
      (gap * parts / 3).wait;
    });
  }).play;
)


