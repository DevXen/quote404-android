#!/usr/bin/env python3
"""Translate bashim chunk_13 batch 8 (input lines 801-900). ru is read verbatim
from the input file; only en translations are embedded below."""
import json

EN = {
394973: """Discussing "I Am Legend":
- I feel so sorry for the doggy!!!....
- That's the whole point, the lead role in the film is Will Smith's, and everyone feels sorry for the dog, not him...""",
394974: """xxx: Over the weekend dad stuck a photo of a Nisan X-Trail into his wish album. And yesterday he crashed into it...""",
394975: """her: damn, I need to go on the net..
him: you should've gone at home!!""",
394976: """1(19:08:25 18/02/2008)
how do you lie in English?

2(19:08:49 18/02/2008)
how to lie in English?) Over a cup of tea and constantly apologizing)""",
394977: """Nif (22:40:30 19/02/2008)
btw I want to watch Rambo 4
just for one scene
they say his machine gun runs out of ammo in it
and he RELOADS it""",
394979: """Shwonechka: In our military town some handy guys ran a LAN and got the net hooked up. The town's in a forest, internet via satellite. A Nokia 7600 is used as the modem for sending requests... Now imagine, over a hundred people sitting on the net through this phone... Truly, Nokia - Connecting People!""",
394980: """shh:
they're just cheap admins
price/quality

mmarkk:
you can't divide by zero""",
394982: """°•°dword°•° 
sometimes I catch myself thinking that I'm almost 20, and I'm sitting here watching Spongebob))))))))

Filo 
Ha!!! Reading your message, I realize we were born on the same day, so I'm also around 20, but our interests are different.. 

Filo 
I love Woody Woodpecker""",
394983: """Gryubocheg 
at our department there's a stand - on the stand a photo of the Tu-154 that crashed near Donetsk (after the crash, i.e. pieces of metal scattered over an area), and a photo of the remains of the An-140 that crashed in Isfahan

 Gryubocheg 
WHAT PERSON CAME UP WITH THE IDEA TO NAME THIS STAND "SCIENTIFIC WORK OF THE STUDENTS OF OUR DEPARTMENT"???""",
394984: """Only Russian inventors could name an amorphous-alloy production unit "Crystal\"""",
394985: """xxx : 
a city dweller in a trolleybus is stalked by 3 dangers:
1) a granny getting off the trolleybus. she is ready to spend several minutes overcoming the 3 steps leading from the cabin to the stop, blocking the exit for other passengers
2) a granny getting on the trolleybus. such a granny starts moving right after the doors open. she couldn't care less that half the trolleybus is trying to get off toward her. until she gets on, not a single living creature will leave the cabin
3) the most horrible. two grannies, one of whom is getting on the trolleybus and the other getting off. in the picture of their confrontation there is something apocalyptic, they're like two giant excavators trying to push each other through""",
394986: """Kerae 
is it this particular moment that puts you off about oral sex?
Kerae
the unnaturalness?
TassadarT
Well yeah... I'M A FUCKING PROGRAMMER AND I'M USED TO THE IDEA THAT IF IT'S A VIDEO CARD SLOT, YOU DON'T SHOVE A SOUND CARD INTO IT...""",
394987: """Melioro: Damn, reading in my art history notes: Dürer painted Eve nagoy. Took me a long time to get it.""",
394988: """her: am I annoying the fuck out of you?
her: am I annoying the fuck out of you, huh?
her: admit it! annoying???
her: no just say yes annoying and I'll shut up
her: I'LL GET OFFENDED and shut up!!!""",
394991: """Fuuuuuck I figured out who Kirkorov looks like when his hair is slicked back!!! CHEWBAAAAACCA""",
394994: """An interesting thing came to light concerning the internet sect "Vkontakte".
If you go to their main page, you can see an inscription like "There are already 7 814 440 of us.", and the number keeps increasing periodically, giving the visitor the feeling that literally (on average) every second a new adept joins the sect. However, one smart person decided to look at the page source. And here's what's written there:

<script type="text/javascript">
 var memCount = 7813477;
 var memPerSec = 0.916437040529;
 function updateCount() {
  next = -(1000 / memPerSec)*Math.log(Math.random());
  memCountString = '' + memCount;
  len = memCountString.length;
  memCountString = memCountString.substr(0,len-6)+" "+memCountString.substr(len-6,3)+" "+memCountString.substr(len-3,3);
  ge('memCount').innerHTML = memCountString;
  memCount = memCount + 1;
  setTimeout(updateCount, next);
 }
 window.onload = function(){
  updateCount();
 }
</script>

...

<p>Нас уже <b id='memCount'>7813477</b>.</p>""",
394995: """<Gun$_tm> the government does take care of its citizens after all!
<Gun$_tm> saw a TV report about a dude who's already 2.6 meters tall and keeps growing...
<Gun$_tm> on that account he's on disability and got an "OKA" car from the state...""",
394996: """bim:
do you know why laundry always ends up inside the duvet cover when washing?

keymaster: 
I know. Back when there were agitator machines, the screw would catch some piece of clothing 
and tear it to shreds. That doesn't happen anymore, but laundry is scared and hides in the duvet cover out of old habit.""",
394997: """Bondar: we once played a city tournament of ChtoGdeKogda
Bondar: they handed out C++ code as questions and asked something like "what does this code do"
Bondar: we didn't work on the question, we were laughing at the comments and trashing the crookedness of the code. There even rounding was done by taking the first two digits.
Oleg™: hm. And who's the author of the question?
Bondar: some Vladimir T-ov
Oleg™: I'll kill the faggot as soon as he gets back from lunch. The code is mine.""",
394998: """<_1313> turns out, Lolo Ferrari is not "buahaha red car", but a porn star""",
395000: """xxx: Hey, what are you doing?
yyy: working
xxx: working what?
yyy: Studying a training manual for support
xxx: what manual?
yyy: It's called The IT Crowd""",
395001: """Arguing with an admin is like headbutting a dragon. You've already kicked him, and poked him in the eye with a sword. Even got the hang of slitting his throat, but then he got bored, and he exhaled...""",
395002: """Ksenia Sobchak is Paris Hilton translated by Goblin.""",
395003: """<Punk-Grunger> hm.. the server I'm writing won't load. They ask me to look into it. I ping... sure enough, silence)) Through a few simple manipulations I find out the hosters are being DDoSed. I ask the tech director to sort it out. He calls the hoster, only manages to say "hello", then his face falls... hangs up, says "they said they're being attacked by Decepticons\"""",
395004: """Like a true master of horror, Lucio Fulci knew that the number and length of scenes with zombies directly depends on the number and length of scenes with Tits. And he didn't need any exploding helicopters!""",
395005: """Rossiyskaya Gazeta: ...which launched some "bots" into the computer networks of British banks and offices, which is "boats" in English...""",
395006: """A female employee is looking for Roman numerals on the keyboard 
advised her to switch to the Italian layout :))""",
395007: """<coredump> security at the data center is fucked
<coredump> I call and say "hello, please reboot our server. the server is such-and-such"
<coredump> they go and reboot it
<coredump> who I am, who I'm from - nobody gives a fuck
* coredump fights the temptation to ask them to reboot another one""",
395008: """Makhh
sneezing with a full mouth of coffee at negotiations is nothing.
I once sneezed while rolling the only joint in a company of eight students""",
395009: """sub0mk: when the state wants something from you it calls itself the Motherland""",
395010: """March: so where's your girl?

Kane: she's mad at me...

March: What for??

Kane: My second girlfriend told my first girlfriend that I also have a third girlfriend :(""",
395011: """X: what's a stable state like?
Y: it's when there's stable inflation, stable growth of food prices, stably unaffordable housing prices and stably high crime...""",
395012: """$kyn$: Today the department pulled a good one
$kyn$: they hung a poster on the stand saying "Happy Defender of the Fatherland Day", and it took up all the space
$kyn$: so the list of those up for expulsion over winter session debts was hung ON the poster =)
$kyn$: all the guys immediately started moving on their debts )))""",
395013: """hrivelote> but you, obviously, live near the cemetery.

hrivelote> you guys probably have zombies casually riding the trolleybus over there.

dimych> riding? they're driving!""",
395014: """xxx:
what was it called in the film?
a quieted eagle-owl, a sleeping hippo?

yyy:
a fox pissing on the right slope of Fujiyama )

xxx:
no, that wasn't its name

yyy:
a fox pissing in the shade of a sakura on the right slope of blessed Fujiyama?

xxx:
a striped tiger, a winged dragon...

yyy:
crouching tiger, hidden dragon
but I like the hippo version better 

yyy:
a masturbating hippo, lustfully gazing at the fox pissing in the shade of a blooming 
sakura on the right slope of blessed Fuji

xxx:
a beaver sleeping in a rock garden, dreaming of a tiger creeping toward the hippo masturbating at the hidden dragon, lustfully gazing at the fox pissing in the shade of a blooming sakura on the right slope of blessed Fujiyama, at
the beaver sleeping in a rock garden""",
395015: """From a Moscow State University student's coursework:
The electronic part is isolated from external electromagnetic noise by a metal casing in the form of a cookie box. Thanks to this improvement the unit not only works excellently, but also looks appetizing.""",
395016: """xxx
human meat tastes like chicken, only sweeter

xxx
oops)

xxx
wrong chat))

yyy
hoooly shit
*contact deleted itself*""",
395017: """vesti.ru: "Swedish surgeons removed an appendix through the mouth"
who was talking about "tonsils through the ass"?""",
395018: """A note at the end of a cell operator's consultant-filled technical ticket:
"The subscriber is savvy, can't bullshit him\".""",
395019: """damaaamn... how am I supposed to understand what happened to the user if he says (quoting): "my mouse is sveristit"?""",
395021: """XXX:People will vote for Medvedev if only because he has a funny surname.
YYY:What is his surname?""",
395022: """Today I watched this scene, riding back from my girl's... at Perovo a rather pretty girl with a broom gets on.. sits down opposite... all 2 stations she rode I wanted to ask her:"What, is it broken?\"""",
395023: """Max: ......The one thing I really can't economize on is cigarettes.... No matter how I try to quit, it doesn't work..... Like you walk to work in the morning, see a kiosk, you'll definitely go up and buy cigarettes.......
Timur: Damn...... Well you said it..... It's like: "I won't jerk off anymore, no, I won't... you pull your underwear aside, and there's a dick....." """,
395024: """xxx> It's good that you got used to the new keyboard with the non-standard layout I gave you so quickly!!! I'm very glad=)

yyy> Агга1 Я толжне очсенбь ражд!1!""",
395025: """Maruselka: Yuri, I congratulate you on Defender of the Fatherland Day!!! Be a real man, healthy, handsome, brave!!!!!!!!! So that a woman can always lean on your shoulder!!!

Uhryab: Oh thanks! I'm delighted! nobody ever wrote me such kind, warm and tender words)
A stingy man's tear rolled down my cheek... I'm touched... touched without unnecessary pathos... Showing off is beside the point here... 
A simple and heartfelt congratulation is often more important than a thousand gifts... A kind word is pleasant even to a cat, and to me all the more!
A million words can't replace this sincerity for me...
Too bad, fuck, that I'M NOT YURI!""",
395026: """<xxx>: A man without internet is a beast!
<yyy>: Nah, a man who paid and is waiting to get hooked up - that's a beast!""",
395027: """the surest way to find out what people think of you is to tell a lamer that messages sent offline don't arrive. you can say all kinds of nasty stuff like that and not be heard. 
that's how I got married :)""",
395028: """222:
people like you in ancient Sparta were thrown off a cliff!!
111:
better off a cliff. and those who remained got fucked in the ass since childhood
222:
Fuck, Max, I didn't know.. sorry...
you'll get your revenge on them yet""",
395029: """doesntmatterwho:
came across a discussion of "how to transport a dog in a car"?
a genius phrase:
"I transport the dog in the trunk, because sitting on the back seat it keeps licking the kid and taking away his toys.\"""",
395030: """Maniac: when I was little I wanted some tea, but the tea at home had run out
Maniac: I look, there's slimming tea, decided to drink it
Maniac: by the time I realized how you lose weight from it, it was too late :(
Maniac: now I know where they got the voice acting for the Airwolf series...""",
395031: """1: the Americans are going to shoot down a satellite tomorrow
2: our guys should try to accidentally shoot down the rocket they'll use to shoot down the satellite. then apologize...""",
395032: """- Work, work, go away to Fedot, from Fedot to Yakov, from Yakov to everyone...
 - Salary, salary, come from Kondrat, come to us from Yakov, come to us from everyone...""",
395033: """her: what do you do for a living?
him: I fix cars...
her: what's there to fix?! a wheel and two handles...""",
395036: """Hally: Sitting with a friend in a bar, drinking, as usual, talking about life. I'm telling him I bought a car, only the generator needs replacing.
Den: what's the problem, we'll buy one, only who's gonna install it
Me: no problem, friends will help, and it's just two bolts to fasten...
A voice from the right: Three bolts.
Us: 0_0 0_0
we turn our heads and see a cute girl peacefully chewing her sushi.
Us in chorus: what did you say?..
Girl: I said "three bolts". The generator is mounted on three bolts - she said, jingling her BMW keys.""",
395037: """Just heard this gem from the programmers in the cafeteria:
"Backup is an act of cowardice." 
Three people choked on their coffee...""",
395038: """xxx: Slava just told me at recess that they had a woman teacher at their lyceum with the surname Poyezd. Know why she quit? 

yyy: no)), why?

xxx: He drew rails on the wall from the teachers' lounge to her office)""",
395039: """I've got lots of moron friends but you're the best of them...""",
395040: """Lead programmer of the programmers department:
I'm not very interested in how to fix it, I'm interested in what the fuck this is...""",
395041: """Neska: what the fuck! Error ORA-06502: PL/SQL: : character string buffer too small numeric or value error

Oleg: translating into yours: miracle-system: too many letters! Couldn't handle it!""",
395042: """Dragon 
and I saw a game yesterday....

 Dragon
like there's a world... and you can walk around it and talk to people

 Kvestor 
that's you going outside))))))))))))""",
395043: """Vovik: Just now was fucked....
Went to the pot. And in our toilet there are 3 pots separated and each has a door of course, and the acoustics are good... Sat down.. Thinking what to do, took out my cell, wanted to determine coordinates with TomTom, and this piece of shit joyfully, to the whole toilet: YOU HAVE ARRIVED AT YOUR DESTINATION!!! Quiet giggling in the neighboring stalls......""",
395044: """<Whirl> and I finished with a guitar solo.
<Aminatep> Misha, hitting the drummer on the head with my "Stratocaster" is not a guitar solo.""",
395045: """Heh.. The pre-election debates are on RTR right now. Well Zhirik got worked up as always, spewing all kinds of bullshit there, went quiet for a couple seconds to catch his breath and then you can clearly hear the operator cracking up in the background...""",
395046: """Rita: fuuuuuck
Rita: mom wrote "DICK" on the TV again
Rita: that's so I'll wipe the dust""",
395047: """Sitting and bitterly looking at a stack of books about 20 in total, and they're all about microprocessor design. Lost in thought I utter: "You've got to put either your life or your d%ck into this". Dad snuck up from behind and said: "Trust my experience - the second one is better\".""",
395049: """From a 3D news review: Out of spite we decided to burn this memory and fed it 3 V (with the nominal Vmem = 1.8 V). This is quite justified, since in 2008 we hadn't burned anything expensive yet. Unfortunately, the GP1066D264L5/2GDC memory didn't burn, but started working at DDR2-1368 MHz.""",
395050: """<Nub> Can someone explain how cells divide?
<K4rli> o
<K4rli> 0
<K4rli> 8
<K4rli> oo""",
395051: """Yesterday I went to an exam... I walk up to the teacher and hand over my student record book. he opens the record book and stares at me with huge eyes for a long long time and goes: want me to give you change too??? and kicked me out:(
(it turned out that when I shoved the record book into my back pocket a 10 RUBLE bill got stuck in it!!!) and I didn't get what the joke was for a long time(( """,
395052: """richter: Fuck this fucking glamour! Let's go to "Rodnik" today instead
In 5 minutes we'll pick up two underage girls, get them harshly drunk on beer and take them to the nearest stairwell, like in the old days! And there we'll fuuuck the hell out of them!))

nadim: yeah, and they'll be watching...)""",
395053: """The project manager (jokingly, with an Estonian accent):
- Are the t-e-e-xts for the site re-e-a-dy?
The copywriter's and editor's answer:
- Part-i-a-lly
The project manager without changing the accent:
- Then you'll get a k-i-i-cking...""",
395055: """It was in the dorm. Someone mixed analgin with hydroperite and put it on the radiator ;) Night, fire alarm, chaos, they went to check if everyone evacuated or not... In one room a dude sits at the comp and with a happy mug gives out a genius phrase:"Oh fuck, if only the speed was always like this... one channel and all mine ;) I ain't going anywhere..." O_O""",
395056: """So we went to a tourist base, there's this one guy who traditionally got hammered. At 5 am he was running around the building yelling, cursing everything out. A granny walks up to him, apparently a local, and dead serious: "What's with the swearing?" He was taken aback at first by such an accusation, looked at her with honest eyes and blurted: "I'm fucking amazed myself\".""",
395057: """Her
for dark hair has always embodied masculinity, and light hair femininity, that's why most girls prefer brunettes 

Him
*threw his head back and admired his manicure* 

Her 
*hawked onto the floor* =D""",
395058: """H849HH  :
What are you doing?

M@treshk@ 
you won't believe it.... waiting for a child=)

H849HH  :
Long?

M@treshk@ :
15 minutes

M@treshk@ ( :
he'll come to English now)))""",
395059: """Sitting, working. A lady from the advertising department runs in and with panic on her face addresses the boss: "What do we do?! We're out of white paint in CorelDRAW!.."
When the hysterically laughing employees ran out of ideas where to get white paint for Corel, the boss put a fat period: "Go, work. I'll send the admin now, he'll buy some and pour it in.\"""",
395060: """Blondy_sterva: Imagine, I accidentally found out that in Tanya's contact list I'm saved as "Bitch"!!!!!!!
Max: what a bitch...""",
395061: """Stanislav Igorevich: A new way to freshen the air!
Deathseller: which?
Stanislav Igorevich: I took the cat, sprayed it with deodorant, and now it walks around the apartment spreading a delicious aroma everywhere %)""",
395064: """<NeuS> I'm sick. tried everything.. maybe you can advise something? something exotic
<alex1> tried eucalyptus in your annus?
<NeuS> WHAT ?
<alex1> fuck!!!! *a bath with eucalyptus.....""",
395065: """Mantius: And when I move my speakers to the new apartment, so that the tenants grasp the full depth and hopelessness of the situation, the first night I'll play only one song: "A wonderful neighbor has settled in our building\"""",
395066: """xxx: You and Mashka remind me of resources from Warcraft.
yyy: What do you mean?
xxx: She's gold, you're lumber.""",
395067: """Shutevsky (01:23:29 20/02/2008)
like a fool I'm making plans, telling everyone I'm busy tomorrow, thinking about what flowers she likes

Insanity™ (01:23:47 20/02/2008)
I don't like flowers at all, I like lilies

Shutevsky (01:24:06 20/02/2008)
oh right, lilies are mammals""",
395068: """xxx : Do you play BK?

yyy : What?

xxx : Fight Club... it's a browser game

*xxx was kicked by admin zzz
reason(First rule of Fight Club - don't tell anyone about Fight Club)""",
395069: """they say that if you remove the profane comments from the Linux sources, you can free up a little over 20KB""",
395070: """<zybr> folks! who has the movie CLOVERFIELD in normal quality? I downloaded some harsh pirated copy, the camera is shaking badly there(((( """,
395071: """1: what was going on at your place last night? 
1: could hear it two floors up
2: There's nothing scarier than once a month.""",
395072: """1: the communist party has no future, it's outdated. 
2: yeah, but they'd win any election if they renamed themselves the USB party.""",
395073: """xxx: that's nothing...
xxx: want me to explain the principle of Wikipedia?
yyy: go ahead
xxx: 
imagine, a dude creates an article with this text: "A sandwich always falls butter-side down".

in a week the article looks like: "A sandwich - a piece of bread spread with butter, always falls buttered-side down. Cause, tendency, hidden properties."

in a year: "The Sandwich Theorem. A sandwich always falls buttered-side down. Being dropped perfectly vertically, a buttered sandwich may with equal probability fall on either side (in fact, sandwiches fell more often on the non-buttered side, since in the process of being buttered they acquired a slightly curved shape). However if you push a sandwich off the edge of a table (the typical everyday situation), it usually makes half a turn in the air and falls butter-side down. This theory can be computed by the formula: the square of the angular velocity of rotation equals the product of the quotient between six times the sandwich's weight and half its length times the width, divided by the sum of one and the square of three times the width of said sandwich\".""",
395074: """1: Enough with the ruler and stuff!
1: Or your grandson will be asking:"Mom, mom, tell me about the pvp paladin Ilya Muromets with the rogue Nightingale!" And you to him "SoloVEY attacks sonic blast IMuromec 16+5=21 (DC 32) -resist
1: Or "and the cat said: so, you give me C-grade boots, and I give you Aden castle at the end of the week"
2: Heh
1: And he rolled off to grind on rabbits on wolves on bears, until he ran into the raid boss Fox
1: What, Danila the Master, can't craft the stone flower?
2: Convinced...""",
395076: """darkSpirit
downloaded a 3D map of Novosibirsk

 mixator 
Stas, you'll probably be amazed by my question, but why the hell are you downloading a map of Novosibirsk if we live in Zaporozhye?!

 darkSpirit
there are no other 3D maps""",
395077: """From the news:

[22.02.08]  On February 20 operatives of the city police department seized about 10 tons of suspicious beer snacks in Novosibirsk.

Comments:
Vadim 
22.02 09:34

Nicely prepared for the holiday! All that's left is to find ten tons of suspicious beer :)""",
395078: """MacroS: The only organs that grow throughout a person's life are the nose and ears.
Ulitka{male}: Yeah, and Duncan MacLeod, when he falls asleep, covers himself with his ear for the night.""",
395079: """<Roleg> The first country to recognize Kosovo's independence was Afghanistan.
<Roleg> the heroin producer recognized the distributor's independence""",
395080: """zzz (23:13:44 21/02/2008)
I'm sick, I have a hellish temperature)))

 xxx (23:13:52 21/02/2008)
what?

 xxx (23:14:02 21/02/2008)
66.6?""",
395081: """xxx: How to say "pizdets" beautifully and scientifically?..
Arcuen: Collapse? =)""",
395082: """her: is it worth buying 450-ruble pills for three sex acts a month?
although... pregnancy is more expensive
her: 1 pill costs as much as one condom
him: do you want to save me money?
her: I want to increase profitability""",
395083: """her: do you have a cable like the one that's tynts-tynts on one end, and on the other end - one of those pluggy things into the comp?
him: no, I haven't bought it yet. 
her: you got what I mean? O_o""",
395084: """xxx: Well, and our profession isn't easy either. No wonder Stephen King's scariest book is called IT!""",
395086: """The importance of a lecture at uni depends on which number it is...""",
395087: """Someone complained here:
-you can't imagine HOW hard it is to even smirk when the boss starts retelling stale jokes from Bash - he's sure my access to Bash is cut off...""",
395088: """madsobaka  (17:54:10 24/02/2008)
my little sister came to visit, she's 4 years old


 madsobaka (17:54:14 24/02/2008)
and so basically she started whining that she urgently needs to play on the comp


 madsobaka (17:54:28 24/02/2008)
but my Lineage is unpacking


 madsobaka (17:54:53 24/02/2008)
so my brother turned on an animation on the phone where a kitten dances and locked the keyboard 


 madsobaka (17:55:05 24/02/2008)
she said why does the kitten only move like that?


 madsobaka (17:55:17 24/02/2008)
I show her the clock - Lera, see, you've got 1749 points here)


 madsobaka (17:55:30 24/02/2008)
when you get to 2100 - it'll move differently


 IvanShurgalin (17:55:34 24/02/2008)
mmmm)))))))))))))


 madsobaka (17:55:36 24/02/2008)
she's sitting there racking them up..)""",
}

def main():
    src = open('chunks/chunk_13.jsonl', encoding='utf-8').read().splitlines()
    batch = [json.loads(l) for l in src[800:900]]
    assert len(batch) == 100, len(batch)
    ids = [r['id'] for r in batch]
    assert set(ids) == set(EN), (
        'missing:', sorted(set(ids) - set(EN)),
        'extra:', sorted(set(EN) - set(ids)),
    )
    out = open('out/chunk_13.jsonl', 'a', encoding='utf-8')
    n = 0
    for r in batch:
        en = EN[r['id']]
        assert en, r['id']
        out.write(json.dumps(
            {'id': r['id'], 'ru': r['text'], 'en': en},
            ensure_ascii=False) + '\n')
        n += 1
    out.close()
    print('appended', n)

if __name__ == '__main__':
    main()
