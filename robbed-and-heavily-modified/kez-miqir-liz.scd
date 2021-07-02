(
  s.options.numBuffers = 8192; 
  s.boot;
)
s.plotTree;
s.meter;
s.scope;

Ndef(\akj).play
Ndef(\akj).fadeTime=20;
(
Ndef(\akj, {
	var a=440, fis=a*16/19/2, b=246, gis=415.30, g=392, snd, env;
	a = [a, a * 1.007];
  gis = [gis, gis * 1.009];
  g = [g, g * 1.015];
	snd = LFPar.ar(g) * 0.4
	+ (LFSaw.ar(fis, mul:0.1))
	+ (LFPulse.ar(
      b * SinOsc.kr(0.45).range(0.975, 1.025),
      width: LFNoise1.ar(7.2).exprange(0.2, 0.6),
      mul: SinOsc.kr(1.2).range(0.1, 0.2) * 0.7
    ))
	;
	
	snd = Splay.ar(snd);
	//snd = LeakDC.ar(snd);
	snd = snd + (Greyhole.ar(
    in: snd,
    delayTime: 0.83333333,
    damp: 0.7,
    diff: 0.56,
    feedback: SinOsc.kr(0.1125).range(0.8, 1.0)
  ) * 0.7);
  env = EnvGen.ar(
    Env.perc(0.555555556, 2.5, curve: -1.0),
    Impulse.kr(0.15)
  ) * 0.5;
  snd = snd * env;

	(snd * 0.6 + (snd.wrap2 * 0.04)).softclip;
})
)
