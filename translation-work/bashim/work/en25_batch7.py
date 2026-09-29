#!/usr/bin/env python3
"""Translate bashim chunk_25 batch 7 (input lines 601-700, ids 407674-407773)."""
import json

EN = {
407674: """xxx: Has anyone tried brewing beer themselves?
yyy: A friend of mine tried. Got some book with recipes somewhere, did everything exactly per recipe, infused it for a long time etc. It turned out, he says, a very good and absolutely non-alcoholic laxative. Didn't experiment anymore.""",
407675: """xxx: what do you picture when you hear "bubble"?
yyy: sorting
yyy: damn""",
407676: """xxx: Why is your extension cord lying on the floor?
yyy: It doesn't fall off it""",
407677: """from a forum:
XXX: Selling a used "Stinol" fridge - 107 for 3000 rubles. Big - two-chamber. The heating elements burned out. Repairmen said it costs about 3000 rubles. Don't want to repair since I got a new one.
YYY: So now it can't heat food at all???""",
407678: """KeyZero: Overslept again?
AzTeK: Yeah... Lost my cell somewhere and couldn't find it in the dark.
KeyZero: Why not set an alarm?
AzTeK: That is my alarm.
KeyZero: Well. Could've searched with a flashlight.
AzTeK: That is my flashlight. :{""",
407679: """- Sanya, I'm moving out of home, help me move, it all won't fit in mine:(
- OK, how much is there?
- 700-800 gigs...""",
407680: """mi3ch: miracle! miracle! the air has become invisible!""",
407681: """xxx: A girl brought her work book to copy - the first entry "Hired as chief specialist (15 times)" xD
xxx: not the most fortunate abbreviation of the word "rank\"""",
407682: """xxx: and she also likes football and motorcycles. next to her I feel feminine""",
407683: """xxx: you can't point out their mistakes to parents :(
yyy: I don't like pointing at myself with my finger either )""",
407684: """@NikitaV: I think the head of the Russian state who started a blog will go down in history as LJ-Dmitry.""",
407685: """<Нотаха> our programmer had a second tooth pulled in a week
<cerr> Did he write code that badly?""",
407686: """xxx: In StarCraft I had 4 last robots with the Terrans (earthlings). Now one's gone, can't find it. Did my troops drink it away?""",
407687: """xxx: And who can create a user?
yyy: A user can be created by a woman, if her man has the appropriate access rights assigned!""",
407688: """Melom@n'Ko: why doesn't anyone believe in plain truths???
Melom@n'Ko: if a file is called snatch.exe, even a normal person's ass should sense that this file is trying to STEAL something!!!""",
407689: """From a forum:
xxx: At the employment center the unemployed can get 58800 rubles to open their own business. WHAT CAN YOU OPEN WITH THAT MONEY???
yyy: With that money you can discover for yourself two weeks of Eastern European beauties =)""",
407690: """mom writes in the morning on ICQ.

marina: the night was terrible
marina: but didn't wake up from the smoke. woke up from the sound of regularly falling metal rods on asphalt
katerina: and what was that?
marina: it was the night shop saleswoman twirling a hula hoop
marina: I'm going to bury her today. the bitch""",
407691: """*xxx was in the sysadmins' room. saw hanging on the wall: a printout of Windows keys and a black-and-white twisted-pair crimping diagram without color labels. and a kettle plugged into the ups. thought a lot...""",
407692: """a call to tech support.
 - Hello, my computer says when shutting down: "You don't have sufficient rights to shut down the computer...". What to do?""",
407693: """Antonio: damn, through American servers found an owner for the building cat in the building across the street""",
407694: """TheDark: buy me a car
TheDark: and I'll drive you around
Henk: there are cheaper ways to commit suicide""",
407695: """[troianS]: at our stand that experiment ran for several days, a whole huge panel was being tested for strength. So, it broke sometime at night, when only a locksmith Seryoga was on duty at the console. So this morning we come to him and ask, like, tell us how it all happened. So he told it in three words...
[troianS]: says, "first everything's fucking great, then wham - and it's fucked". But the best part is that from these three words we now have to write a report to the ministry.
[troianS]: that's where science begins, damn...""",
407696: """xxx: how was vacation?
yyy: great, recommend it
yyy: When vacation comes - find any kids at all, whatever, to take with you. After such a vacation you'll run to work like it's a holiday.""",
407697: """Her: If the neighbors yell and wake the baby, knock on the wall for them.
Him: What? With the baby on the wall???""",
407698: """xxx: you're kind, that's good, that'll come in handy in life...
yyy: for others!""",
407699: """Kantemir: GOOOOOOOOOOOOOOOOOOOOOOAL! Wellington scored a brace! Alex gave the ball to Ibson, he quickly switched to Wellington. He got past Rodolfo and confidently beat Guilherme.
Kantemir: Harsh Russian football....""",
407700: """Thinking of setting my password recovery secret question as "The Main Question of Life, the Universe and Everything", and making the answer not 42. Just imagining the hacker's dialogue with support:
- Name the answer to "The Main Question of Life, the Universe and Everything".
- Answer: 42.
- Incorrect.
- How "incorrect"?!""",
407701: """from lj
vuk:
And where and what alternative takes would you add?
sergthegod:
in "The Lord of the Rings". Sauron forged the ring, tripped and dropped the ring into the mountain's crater. And that's it, no fairy tale""",
407702: """arxont: Sitting, going through source code inherited from my predecessor
arxont: Come across the function XXLSEXPORTDIALOG?
arxont: Sitting and thinking what it does - "TFR XXL SEX PORT DIALOG" or "TFRX XLS EXPORT DIALOG".
arxont: Judging by the code, the first one""",
407703: """Seamni:
Either I've gone crazy, or this world has. Right now there's a bunch of gopniks hanging out under my window. Wearing caps, tracksuits. You think they're drinking? No! They're dancing to the song from the movie "Gentlemen of Fortune\"...""",
407704: """xxx: today already performed a labor feat
xxx: before lunch they called - the complex isn't working. I said I'd look and forgot
xxx: now they call - say thanks, everything works
xxx: you're welcome, yo""",
407705: """xxx: you're going to driving school soon?
yyy: at 11
yyy: I'll be torturing the instructor...
xxx: will you light matches between his fingers and drive needles under his nails?
yyy: I'll just slowly burn his clutch....""",
407706: """xxx: I wanna wash my head. but I'm lazy
yyy: don't wash
xxx: what, not yours?
yyy: don't wash your head, idiot""",
407707: """A conversation between the "head of the programming department" and the chief tester:
T: Did you see what these hotheads cooked up?
P: Where exactly?
T: In the mail client, you gave it to me to test.
P: If you mean that the program refuses to work with a file attachment if the file name has Russian letters, I'm aware of that.
T: I'll tell you more, it refuses to work with a file attachment even if there are Russian digits there. And especially - if there are Russian spaces.
P: Just don't tell me how you found that out, OK?""",
407708: """x: you know there are interesting chess sets - with cats on one side and dogs on the other.
x: or like Kutuzov's and Napoleon's troops,
x: AND I'LL FUCKING MAKE ADMIN VS ACCOUNTANT ONES!""",
407709: """News on LOR:
The codename for Ubuntu 11.04 will be Natty Narwhal. translates as "neat narwhal".

Comment:
Ordered disks by mail,
Carefully tore them up,
In the morning threw them in the basement.""",
407710: """equals: And we broke up on New Year's. We agreed in advance that the gift would be a book. She gave me the Kama Sutra, and I gave her Rozental.""",
407711: """Dementor
Already said. The comp with the dead hard drive that's not in use right now has a monitor. If nobody took it, I'll use that %)

Dementor
will need to call that dude in whose room it stands...

Krond
Don't call, make it a surprise :-D

Dementor
Nah, I already told the boss about this comp.

Krond
Too bad. Imagine, a guy's working, suddenly someone bursts in, grabs the monitor and runs away :-D

Dementor
That happened at our place recently :-D

Dementor
Someone's monitor died too, so the head admin found out who's on vacation right now, took the monitor from that comp and installed it. Next day the head of that department comes in and freaks out - "AAAAAAA they stole the monitor from my department!!!11111\"""",
407712: """Ulyana Miloradovich:
saw a picture book for unhappily in-love women. In it she lies with a knife in her chest on the table, while in another den this dude and his friend are selflessly cutting into the computer with indescribable delight

Sergthegod:
heh )
nah, I'm not like that

Sergthegod:
and what are they cutting into?""",
407713: """xxx: So, how are things in Chelyabinsk?
yyy: 55 degrees
xxx: O_o
yyy: northern latitude""",
407714: """Smirnov™: a pity that out of ten girls, statistically nine are dumb
Smirnov™: statistics don't lie
katka))): don't lie about what?""",
407715: """shlavik: kids' screams from the street just tore my brain to shreds with the phrase "free money for sale!" %)""",
407716: """xxx> well she used to draw UI
yyy> on fences?""",
407717: """xxx: Fought the router for three days.
xxx: Turned out I just needed to pay for the internet )""",
407718: """News discussion:
About 14 thousand years ago mammoths were forced to retreat to present-day Northern Siberia and leave the regions of Europe, including Great Britain, where they had lived for many tens of thousands of years.

mazya: They might as well add that their theory is proven by thousands of skeletons on the bottom of the Channel.
LeMa: Yeah. Rare is the mammoth that swims to the middle of the Channel.""",
407719: """xxx: news on computerra "Fish oil may protect boys from depression"
yyy: right, it drives them into despondency""",
407720: """1 Those were the days... They counted kopecks as money... :(
2 I'll tell you more! There were days when the kopeck was considered a good car!!!""",
407721: """xxx: Maxim, why is the form built so that it's easier to retype the values than to copy-paste?
yyy: Well you're supposed to retype, what's the problem?
xxx: You probably don't use your turn signals when turning either?
yyy: How did you know? And what's that got to do with it?""",
407722: """xxx: where did you leave the room keys?
yyy: where I always do!""",
407723: """xxx: Today saw one kid pay with money from his phone in the store.
xxx: Opened the phone cover, and he's got 500 rubles in there, took it out and paid.
yyy: It is the third millennium after all.""",
407724: """xxx: hi, can you recommend where to take a stroll at night in Moscow?
yyy: with what outcome?
yyy: light injuries, grievous, lethal?""",
407725: """xxx: Husband went to take out the trash. Came back with a strange expression, called me to the garbage chute.
I approach it, and from the pipe - with a tender Chinese voice the song "Twinkle twinkle little star".
xxx: Creepy. I left, sitting in disheveled feelings. I imagine lying there is a bubblegum-pink furry creature singing its song. And apple cores fall on it from above, and it rejoices: oh, an apple! oh, a bun...""",
407726: """Seele: I'm slowly taking off my stockings..
fLAy: from the clothesline?
Seele: Damn
Seele: From the robber's head, damn""",
407727: """<Juggernaught> My HDD burned out, bought a new one yesterday, and all the shared files disappeared!
<Mut> shouldn't have stuffed it with porn.
<Mut> it burned out of shame.""",
407728: """Sunrise: I rented an apartment
Sunrise: Looks like they're going to shoot a horror movie about it
Sunrise: At night strange scraping and sounds like in horror movies, the fridge in the kitchen opened by itself and immediately slammed shut and at that time some girl's laughter in the yard
Sunrise: It's insane))
SToRM: hgg)))) see ya sis!!)))""",
407729: """(about Yalta)
xxx: no, it was VERY hot there, just unbearable.
there I remember there was a sign on the temperature stand: 38 degrees.
then mom says: "well, 38 again". and the woman next to the stand says: "not 38 again, it's 40, I just don't have such a sign" )))""",
407730: """xxx: Listen, I want to buy vitamins for the kid, but forgot the name. In the ad they sang: "For the baby's health..."
yyy: Weed!""",
407731: """xxx: Went to the store to buy a drink. Come out, there's a bunch of punks looking like real ones, afraid to go into the store because, I quote: "Fuck that! we're pathetic!\"""",
407732: """Katya: hm, what happened?
Victoria: exactly!!!!!!!!!!!!!!!!! nothing!!!!!!!!!!!!! nothing is happening!!!!!!!!!!!!!
Katya: forest fires, inflation, mortgage, apocalypse in 2012... listen, you're hard to please))""",
407733: """XXX: how do you make text bold?
YYY: I send it through a McDonald's proxy, through it it gets fatter.""",
407734: """France: What clever captions you have in your statuses and photo comments. Pity none of them belong to you.
Kikban: Cool status! Where'd you copy it from? )""",
407735: """xxx: Misha, being greedy is not good!
yyy: not good for whom? I like it""",
407736: """xxx: my hamster is such a slowpoke, you bring him food, he smells it, crawls out and looks at me pitifully. I put food in his cage, and he just stands there looking at me, like five minutes, only then it dawns on him that the food is two steps away )))
yyy: pets are usually just like their owners.
xxx: yeah, exactly exactly )))
xxx: wait, what?""",
407737: """GR@DU$: just got an email from our admin on corporate mail with only one phrase "everything will be fine..."
GR@DU$: how scary those words are to me""",
407738: """Heard in the morning at the neighbors'. The husband wakes the wife. She replies:
- Wait, five more minutes.
- Sure! Just don't confuse "five more minutes" and "fuck, shit, I'm late again\".""",
407739: """Oleg: I quit work for studies, studies for music, music for health, health for work...
Oleg: the question arises - what for?!""",
407740: """So we drive into the office with the driver, and already in the yard a girl carrying a cartridge for refilling crosses our road. The driver says - well, that's bad luck. I ask "Why?". And he says - "A girl with an empty cartridge crossed the road\"""",
407741: """xxx: Today I'm like at a viewing. All day moms with freshmen barge into the room, and I greet them either in underpants with a string in my hands, or nailing up a Cradle of Filth poster, or pouring peroxide on a freshly pierced and still bloody ear.

xxx: Nobody's moved in yet =)""",
407742: """xxx: So how's the admin job?
yyy: As usual - ass in dust, face in sweat, but at least I'm on the internet.""",
407743: """Ave Luna:
every time I crack up when a gloomy father enters the room and, nodding at the row of stockings on the clothesline, demands: "take down this moulin rouge\"""",
407744: """zzz: in London the transport system is well thought out, though it's even more complicated than Moscow's, but some names!
ddd: yeah?
zzz: for example, we live in Middlesex, but that's not enough
zzz: from the airport the route is -- At Heathrow Terminals 1 2 3 take the Piccadilly line (direction Cockfosters).
zzz: cockfosters! the etymology is clear, everything's decent, but unwanted associations keep creeping in
ddd: the Russian equivalent - the "Blue Horizons" platform""",
407745: """xxx: If it's in your blood you'll learn without a book.
yyy: Yeah right, lab results from the hospital: Patient, your sugar is normal, hemoglobin is fine. java 6.0, c++ prevails...""",
407746: """xxx: Today had an X-ray at the X-ray center. Taught the radiologist to burn images to disk. Got a 50% discount. Afraid to go to the surgeon. What if I can get a discount there too.""",
407747: """xxx: Dima, if you don't stop playing your fucking Lineage, I'll leave you!
yyy: fine, I deleted it.
xxx: Really? :)
yyy: really.
xxx: What are you up to, kitten? :-*
yyy: downloading WoW.""",
407748: """From Habr (discussion of a new probabilistic processor):

AYrm: A breakthrough in interface — recognizing what's happening in the room, voice and gesture control. This is a qualitative leap from the rather primitive stuff there is now. Analysis and prediction of user actions. For example not turning on video chat if the owner wanders the room without pants.

strk: or turn it on, upload to YouTube but not notify him about it. That's up to the processor to decide.""",
407749: """zaza: and otherwise how's it going?
katytakana: fine, I think it's shitty, others say I'm dumb
katytakana: two months ago I fought with daddy
katytakana: he likes retro, so I told him he's outdated and all
zaza: ( and what? still fighting?
katytakana: yesterday at the dinner table he made an idiot face and said "look I'm Katya, I'm dps1" and started making sucking sounds (
katytakana: now he's the raid leader in our guild in WoW (
zaza: ))))))))))))
katytakana: mom's furious
katytakana: daddy eats her brain every day about how great it is to be a blood elf and kill people ((( """,
407750: """xxx: if anything, that was in that happy stretch of my childhood when I had no internet
xxx: music was brought to me on CD disks
xxx: flash drives weren't in fashion, DVD disks either
xxx: but mammoths were already extinct by then""",
407751: """iljich: I remember, in 11th grade I started the introduction to my essay in the trial EGE with the epic phrase "Well, let's start slowly!\"""",
407752: """XXX: today in our building some bastards once again clogged the sewage riser with apples. Again flooded the whole basement, had to call emergency.
XXX: third time this week, damn.
YYY: happy Apple Spas :)""",
407753: """<Kallamish> sitting here, decided to order pizza online
<Kallamish> ordered, sitting, waiting
<Kallamish> something's not showing up, we call their service, and the conversation goes like this
<Kallamish> - hello, we ordered pizza online, and you're not here... why?
<Kallamish> sad voice: we don't have internet""",
407754: """xxx: Listen, roll by me for a couple minutes? )
yyy: Pretty or for business?""",
407755: """a friend decided to do a stunt on a motorcycle

xxx: have you ever done this before?
yyy: no, but I saw it on YouTube and decided to repeat
xxx: I think these words were the last in the lives of more than a dozen people""",
407756: """xxx: our mom totally lost it! go there do this, then come back - report and go do that all day, she's driving me nuts! at least help!
yyy: she's an NPC...""",
407757: """XXX: Hello, I'd like to buy doctor's sausage from you.
Saleswoman: Which one, the doctor's that looks like the milk one in the first row, or the doctor's in the second row that's but looks like the one in the fifth row ?
XXX: An edible one if possible...""",
407758: """Cost optimization Russian-style is to remove the 2.5k ruble bonus from an employee for washing office floors and hire an outsourcing firm to do the same work for 15k""",
407759: """xxx: Anyway, she got a high fever at night and started bringing it down with improvised means!
yyy: with a stick??""",
407760: """Xment: Hahaha! Neighbor with the drill, you're a loser! I woke up 5 minutes earlier than you today!""",
407761: """xxx: I'm probably an idiot, but I can't quack.
yyy: Sorry for the offtop, but if you do manage it and start quacking - do you think it won't look idiotic?""",
407762: """xxx: capslock's driving me nuts!
yyy: Rip it out
xxx: *rips out capslock*
yyy: After that you have to type "BEFORE RIPPING OUT CAPS LOCK I PRESSED IT, WHAT DO I DO?!?!"
xxx: FUCK""",
407763: """xxx: you'll freeze
yyy: I hardened myself
xxx: how?
yyy: ran around the winter map in Battlefield :D""",
407764: """<зубрег> damn, what to delete.. no space left on the HDD at all
<0_0> delete the porn
<snak3> delete the porn
<зубрег> you bastards
* zubregr left the chat
<JacoPs> delete the porn
<JacoPs> oh, didn't make it, huh? =(""",
407765: """Afrika: Rom
Afrika: tell me man to man
Remo: m?
Afrika: only honestly
Afrika: just between us
Afrika: yes?
Remo: of course
Afrika: tell me, do you throw sausages into boiling water or does it not matter?
Remo: )))
Remo: why boil them at all, just eat them)
Afrika: :(
Afrika: you men are all the same""",
407766: """- Why was I so angry before?
- Because you didn't have a mouse for your laptop...""",
407767: """Valery Smirnov: What are you writing so long there?
Valery Smirnov: If you put a rock on the keyboard, it'd scribble something faster.
Valery Smirnov: Daaamn... SO LOOONG...
Valery Smirnov: FASTER!!!
Valery Smirnov: You're fucked, do you have tentacles instead of fingers?
Sema Kishchuk: yes""",
407768: """her: what are you studying
him: programming
her: why
him: to know. Why do people study things?
her: well you know English
him: what's the connection?
her: Tele2""",
407769: """Egor: hung one door
Egor: haven't finished drinkin' the second
Egor: haven't finished
Egor: haven't finished sawin'
Beloved: You sawin' with Pasha?!!!""",
407770: """xxx: I got roasted in the sun today like a grilled chicken. now I'm like a turkey in all senses :)
yyy: an apple in the ass?""",
407771: """xxx: Boss yelled at me again for listening to music in headphones.
yyy: Tell him that even galley slaves got a rhythm beaten for them.""",
407772: """xxx: How can I make messages from ICQ go straight into WoW chat, just in a different color? Really need it! My relationship with my girlfriend is on the verge of breaking!""",
407773: """serginio: Walked along the Elektrozavodskaya embankment, look, ladies are painting a fence. A fence of metal square-profile, tall. Painting with brushes, thin ones, carefully...
serginio: And everything seems fine, but to paint the top part of the fence, they taped the brushes to CROWBARS.""",
}
