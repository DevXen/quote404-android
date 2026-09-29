#!/usr/bin/env python3
"""Translate bashim chunk_25 batch 9 (input lines 801-900, ids 407875-407974)."""
import json

EN = {
407875: """xxx: Grandma in the village couldn't be happier this summer at how neatly I stacked all the firewood in the shed.
xxx: The hands still remember the old Tetris skills.""",
407876: """xxx: Why does a person looking at a sleeping cat get touched, while a cat looking at a sleeping person thinks: "Oh! A trampoline!\"""",
407877: """X: well they're gradually taking over the net :) then they'll leave 2-3 protocols and everyone will be calm :)
Y: I'll tell my grandkids how you could save a picture on the Internet without entering a credit card number, roaming in ICQ was free, and porn banners chased you across the page. And they'll believe it about as much as we believe in 5-kopeck ice cream - like it probably was, but it's so fairy-tale-like, distant and unbelievable.""",
407878: """Faint: listen, I can't figure out - why are you always walking so hunched?
Stew33: aaah. it's just my headphone cord is short""",
407879: """Lena: Help me choose a camera! So it has all kinds of functions... So my hands don't shake.
BorsCh: You need to quit drinking. Or gently get over the hangover.
Lena: No, damn! So the eyes aren't red!!!
BorsCh: Same advice.""",
407880: """xxx: How do you like our site's mockup?
yyy: The designer needs his ass torn off
xxx: why the ass?
yyy: To eliminate, so to speak, all his problems with hands at the root )""",
407881: """<adm> after work, as usual, relaxing in Guitar Hero. then the doorbell. open. on the threshold a guy about 30. I ask "eh?". he goes "I'm the downstairs neighbor. is that your Guitar Hero blaring across the neighborhood?". "well mine I guess" - I answer. he looks at me (a guitar hanging on me). "got a second one"? "well yeah" - I answer, "what?". "my wife sent me to make you turn it down. but since you have a guitar, maybe one song together?". I'm in shock. let the guy in. we play a song. he leaves. now sitting and thinking "what was that?"..""",
407882: """Last Hero (09:51:24 1/09/2010)
Today overslept for work with the thought "how good that I don't have to go to school" :(""",
407883: """x: how do you boil store-bought condensed milk and for how long?
y: boil on high heat in an open pot until the characteristic pop and a newly colored ceiling""",
407884: """<[Real]muskus> well, again: "mom I have problems, urgently put 900 rubles". Somewhere a slacker is growing up who thinks I'm his mom, and he's in trouble again. *sadness*
<scarabay> [Real]muskus: and you'll shatter his dream of a mom? How cruel you are.
<[Real]muskus> scarabay: as a wise mother, I can't deprive my child of the chance to independently climb onto the solid ground of financial independence.
* scarabay delighted: such nobility!""",
407885: """xxx: so I'm riding home today
xxx: look out the window, it's dark. thinking - how late it is already
xxx: then I realize I'm riding the metro""",
407886: """denistnt> I'll never forget how in the 90s, a ticker ad read: "'Squirrel' store sells wholesale and retail liquor-vodka products\"""",
407887: """xxx: yesterday dad tested my endurance.
xxx: said I'm a painted monkey.
xxx: and I replied that I take after whoever I was born to..
xxx: ran 5 kilometers after me with a shovel.""",
407888: """xxx: in my group there are two girls and three glamorous kitties
xxx: 170 kg net of shiny catflesh
xxx: 5 running meters of emptiness""",
407889: """xxx: In the dorm in the hungry years I drank tea gangster-style.
xxx: You take a mug, go to the neighbor girls. "Girls, pour me some boiling water, our Seryoga broke our kettle." They pour.
xxx: You go to the next ones. "Girls, can I bum some tea leaves, Seryoga drank the last tea bag this morning." You get a tea bag.
xxx: To the next you say Seryoga finished the last sugar in the morning.
xxx: And to the last you say Seryoga devoured all the bread for breakfast.
xxx: And they believed.
xxx: Seryoga's like that. He could.""",
407890: """xxx: Who pinged Ulaanbaatar?
yyy: Well, me.
xxx: There's no more of your Ulaanbaatar...

(from the internal correspondence of a backbone provider's tech support)""",
407891: """xxx: Did you go to the sea? with a girl?
yyy: yeah.
xxx: looks like it's serious with her...
yyy: already with another girl.
xxx: damn! and is it serious with this one? )
yyy: nah, so far normal...""",
407892: """Kostyan: bet a neighbor he couldn't shut down or reboot my comp over the network, I have all kinds of programs 8).
Kostyan: five minutes later the monitor went dark - the comp shut down, I'm in shock, the programs don't work (
DayOtvertku: what did you lose?
Kostyan: nothing, I hacked him too, though I had to reboot his fridge, TV and washer along with the comp.
Kostyan: he still hasn't figured out how a simple electrician broke his comp )""",
407893: """her: let's have a third kid?
him: no.
her: why?
him: the toothbrush holder has only four holes.""",
407894: """xxx. Philosophical problem: is it even decent to order wedding icons online?
yyy. Well... If your TCP/IP protocol is consecrated, then it's all OK!""",
407895: """Husband brought a duck from the hunt, threw it in the freezer.
Two days passed.

Me: I think it can't keep long. Need to cook it faster, or it'll quack...
Him: You know, it'll never quack again! :D
Me: :D Well, I mean it'll die soon.
Him: It already died two days ago!!!""",
407896: """xxx: in the 80s I was crazy about Alisa.
yyy: Allow me to clarify, Selezneva or Kostya Kinchev?
xxx: well think for yourself, who would I, a healthy rocker man with a hoarse voice, be crazy about?
xxx: Selezneva, of course!
xxx: nah, Kinchev's not bad either
xxx: but Alisa Selezneva is sacred.""",
407897: """xxx: yesterday we were catching a mouse
xxx: put a 3L jar by the hole, poured some oil in so its paws would slip and it couldn't climb out...
xxx: yesterday we didn't see it there
xxx: but this morning I come in, and the jar's been shat in""",
407898: """xxx: today it's 14 years since I started working here!

yyy: they don't give that much for every murder!""",
407899: """xxx: Hey! How are you?
yyy: hi! 2 floors of office plankton moved in. pulling cables.
yyy: yesterday evening was at the seamstress, saw 2 cut pieces of underwear elastic.
yyy: first thought was - need to find 4 jacks and crimp. started counting how many little elastics are sewn in parallel.""",
407900: """belkino: Now you understand why I'm frantically looking for old Soviet textbooks? To tutor the kid at home, otherwise it'll be +1 to the generation with higher education writing "definately"...""",
407901: """xxx: Kleptomania is the ability to find something before someone else loses it.""",
407902: """Camera discussion.
Poni_v_pympone: Yesterday they were stealing my gravel, filmed it, quality excellent!""",
407903: """xxx: the condom broke :(
xxx: bad idea to write "To Berlin!!!" on it with a pen""",
407904: """xxx: What was your first love's name?
yyy: Dendy.""",
407905: """<NATALI> hi!!haven't forgotten me yet!!I work at a nuclear power plant
<NATALI> someone write
<El-Fierro> Homer?
<NATALI> no not homer
<NATALI> natasha from the nuclear power plant in piter""",
407906: """On the highway near Krasnodar a KamAZ and a milk truck collided. A kilometer-long traffic jam formed.
From the comments: The victims of the accident were thousands of village cats who wore their tongues out on the asphalt.""",
407907: """Anastasia: and here I am sitting, in woolen socks by the heater, enjoying the last hour of summer.""",
407908: """chaika: TV journalists are completely philologically illiterate. I've had to read various intros to stories a few times - it's a total fuckup!

kora_f: I also often make mistakes in articles. Then I reread, and cringe.

chaika: Well, everyone does it from inattention. But you wouldn't write "I like that I'm sick with nevas"

kora_f: I would. It'll be the headline of an article about native Petersburgers' love for the Neva and Malaya Nevka.""",
407909: """xxx: Our boss is a beast
yyy: a goat?""",
407910: """xxx: bunny, tell me what time it is?
yyy: by Greenwich?
xxx: by the microwave, I timed it on it ;)""",
407911: """Chugunkin: eating salted milk-cap mushrooms. my own. searched in the forest with a book.
Chugunkin: if I die - kill the author.""",
407912: """Lyuba: Can I ask you something? THIS IS IMPORTANT!
Slash: You're awesome. You can do anything!
Lyuba: I'm glad. And yet, when I'm standing WITH MY PARENTS don't run to me across the whole square, arms stretched forward, screaming "TIIIITS!!!"
Lyuba: They at least didn't appreciate it...""",
407913: """Max: Writing to the bank "please increase my credit card limit 1.5 times". Reply "state the reason". Me - "not enough". They increased it :-)""",
407914: """<Fokc> brew sedge
<Fokc> and it sounds Japanese
<Fokc> and it grows everywhere""",
407915: """<xxx> "A person can bear anything, if he's not stopped" (C)
<yyy> In the sense "bear" = "endure" or "carry away"?
<xxx> Either )""",
407916: """Lisa: my son made a nightlight out of Artem's skeleton [a plastic model from a magazine]. Took out his brain (literally) and screwed a flashlight into the skull.
Lisa: Says now he's not scared to sleep.
Lisa: I'll apparently never grasp this logic...""",
407917: """soffa: I'm lying on the beach, next to me two ladies, about 100 kilos, and I hear one telling the other: "listen, when we get to Kiev - we need to learn to use Photoshop" )""",
407918: """xxx: Today the meaning of the expression "Got up on the wrong foot" became clear to me.
xxx: This enlightenment came to me in the form of an unexpected and very powerful hit of my nose against the wall.""",
407919: """Trololo: And why aren't you eating?
Feek: Well, I'm fasting, no money till the first paycheck.""",
407920: """§ERbER: who are you?
MiLaHa: a gurl
§ERbER: where'd you learn olbansky?
MiLaHa: in kindergarten
§ERbER: we've lived to see this""",
407921: """X: A new girl appeared in our guild!
Y: Cool, race?
X: druid
Y: Of course. Who'd doubt it. NEs are NEs. Bet she's specced into cat too?
X: nah, heal. Girls usually heal.
Y: They usually heal ass-handedly.
X: dunno. one chick healed me so well I refused to let anyone else heal me at all. healed that well.
Y: I'm hard. Keep writing.""",
407922: """Xxx: So, did Lizka bake the pie?
Yyy: She did, but I didn't eat it.
Xxx: Why? You wanted it for so long. Crumbly, Lizka's..
Yyy: Well you know... She's cooking, suddenly runs into the room, types something in Yandex. Reads, leaves. I climb into Mozilla, and in the search bar the question: "If there are ants and larvae in the flour - can it be eaten?". And the bastards answer that "yes".
Yyy: I decided not to try this apple-meat delight.""",
407923: """assdancer: Know why I didn't get into Linux right away?
assdancer: My first distro was Gentoo. At 11 years old.
Floord: "Passed the driving test, but had to assemble the car myself"? )
assdancer: yep""",
407924: """xxx: If on Friday evening drunk you suddenly iron a shirt and leave it hanging on the chair back, too lazy to hang it on a hanger, then on Sunday morning you'll definitely find it on the floor, with a cat inside.
xxx: And on Monday it'll turn out there are no other clean shirts.""",
407925: """XeHa: know what happens after death?
WarLock: no
WarLock: don't spoil""",
407926: """Ginger tank: How I love our clients!
Ginger tank: They ask "change the commas to dots in the numbers, it's ugly".
7th lvl Conjurer: Let me guess - an estimate calculation in Excel.
Ginger tank: You knew.""",
407927: """xxx: Giving advice, always give up your seat in transport to almost bald, half-gray, pot-bellied, suit-and-tie, glasses-wearing, black-briefcase-carrying uncles!
yyy: Why?
xxx: Because they might turn out to be your calculus professor :-( """,
407928: """gi: you can't lose!
ga: well yeah. I'm not Winamp""",
407929: """Saimon: I feel winter's coming...
Zingafur: How so?
Saimon: Running Crysis on the laptop again, to warm my hands.""",
407930: """From Google Questions and Answers.

Question: Do you agree that believers are kinder and more moral than atheists?
Answer: If nobody's being burned at the stake, then yes.""",
407931: """xxx: I wanted the best...
yyy: And it turned out better not to remember!""",
407932: """Meryk: we bought condoms, I gave them to my husband and went on shopping. my husband's hands were busy, and our baby likes fiddling with boxes, so on autopilot he tossed the box to the baby in the stroller, let him play...

Meryk: and we probably walked around the neighborhood for an hour, not understanding why people were peeking into our stroller in surprise... )""",
407933: """xxx: Invented the ultimate weapon to drive my wife away from the laptop
xxx: You tell your daughter (1.5 years) "bite mommy" - she tears her to shreds.
xxx: She stops when the wife takes her to another room and distracts her with something.
xxx: But there are pitfalls: now I'm woken up in the mornings with the command "bite daddy".""",
407934: """HR: You're not right for us. We need young, ambitious people capable of creative growth!
Elderly accountant: Write down my phone number. When it turns out everyone ambitiously grows but there's nobody to work - call me!""",
407935: """yyy: do you get bouts of laziness?
xxx: I get bouts of activity, laziness is constant with me =)""",
407936: """Hellish Skrouu: how glad I am that in my childhood there was nothing to record video with and nowhere to upload it!""",
407937: """xxx: the truth is in wine!
yyy: whose?
xxx: are you from law school?
yyy: yes
yyy: how did you guess?""",
407938: """xxx: Agent 007.
xxx: Is 007 a number or access rights?""",
407939: """From a forum discussing the news "Mingorispolkom decided to rename all vocational schools of the capital to lyceums"

victor-72: Now in the little square near my house it's not vocational-school kids chugging rotgut before the disco and cackling wildly, it's lyceum students deigning to enjoy wine before the ball, wittily joking...""",
407940: """>Lame =(
>I know the game's cool, but can't play. After starting
>a new game a black screen pops up and no matter how long I wait, just black
>screen and that's it. Lucky you people)
>The comp seems normal, the heaviest games ran, but this one...

Dummy!
Turn on the flashlight)
Everyone has a dark screen there, it's just dark!""",
407941: """xxx: Whatever you call the cat, it still won't swim!""",
407942: """a guy and a girl are fighting
Him: I don't know what to say. I'm a donkey.
Her: Say - hee-haw.""",
407943: """September, retake session, a dude is retaking higher math for the fifth time.

Prof: Horrible! You know nothing! Can you at least ask yourself a question and answer it correctly?
Student: I can.
Prof: Ask.
Student: Fourier series...
Prof: Good, tell me.
Student, thinking: Damn, can I have another question? (""",
407944: """xxx: And my cat has been delighting me for 12 years. And he's also medicinal.
yyy: aromatherapy?
zzz: you brew him?""",
407945: """- What's the fuel consumption of a Lada Kalina?
— Three cars per two thousand km.""",
407946: """xxx: What's "business-class email"?
yyy: It's like regular, only there's more legroom and they hand out free booze.""",
407947: """xxx: Hi, guess what we're having for dinner?
yyy: Hi. Dumplings again?
xxx: How do you do that? :)""",
407948: """xxx: a friend and her husband bought a laptop, connected the net, Lenka registered on Odnoklassniki and today she got a spam message that her photos are posted on a porn site and the address of that site.
xxx: she's been crying all day, looking for who could've done it, went through everyone in her mind, fried my brain and my sister's over the phone, we still can't explain to her what spam is, it just doesn't get through to her
yyy: cave people""",
407949: """xxx: If HP made weapons, the ammo would cost more than the weapon itself.
xxx: And folk craftsmen would quickly learn to refill spent shells with gunpowder and plug them with dug-up bullets.""",
407950: """xxx: should actions performed through force be considered unnatural and therefore undesirable?
yyy: write your term paper and stop whining""",
407951: """xxx: Hey! How's it going?

yyy: well... tormented by doubts. Tell me please, how do I address a wedding invitation to relatives, if they're not dear and especially not respected?""",
407952: """xxx: Sergey Mikhailovich runs our DB. and what do you think they asked me?
xxx: "so, how do you like BDSM?\"""",
407953: """<Маньяк> A 5000-character story for the newspaper? I write longer SQL queries!""",
407954: """<Yevy> _penpen_: just like me. Don't have a PS3, have a friend with a PS3... )""",
407955: """Comments on the movie "Tekken" on a torrent tracker:
xxx: "Genre: drama"
Indeed, with a $35,000,000 budget to shoot such sh*t, that's a drama...""",
407956: """poweedlo: The boss came up just now, wanted to ask something
poweedlo: Saw a dick half a monitor wide, lost the gift of speech
poweedlo: me: "Testing a new antivirus, the keys just arrived"
poweedlo: Him: "Aa.." and left, never asking anything""",
407957: """xxx: In our office pens get stolen often, kleptomania is terrible.
xxx: The dispatcher tied a pen to the keyboard, so it left together with it.""",
407958: """fade2black: looking at apartment ads now
fade2black: clean entrance, parking, intercom, deceased neighbors.
fade2black: good neighbors
Mikhrutka: a good neighbor is a dead neighbor""",
407959: """Zhulanchik: I grasped the full epicness of our country's football fail somewhere around the 2000s, when our local football team was beaten soundly by ordinary conscript soldiers. In kirza boots. With footwraps. In camo.
Zhulanchik: And the footballers as expected - cleats, uniform. But still lost 2-7. And they even played with a ball (even a football one, inflated), not say, crumpled sheets. And played on a field, not a parade ground.
Zhulanchik: And you keep asking "why did ours lose?\"""",
407960: """xxx: What the...
xxx: The one single fly flying around decided to commit suicide specifically in my coffee!""",
407961: """Yary:
At work they gave the org-method department lists of employees living in the central district, to invite them to a rally. at the clinic, I mean. The org-method department found a person to call all employees. The joke is the department gave that person HOME phones. And now they're calling all EMPLOYEES at home and nobody's there - strange, right?


Yary:
She calls from our office, understands everything, but for fun follows the instructions exactly. Somewhere relatives are writing down that their kin are being sought at work. With each call the department weeps""",
407962: """lesnik75: A "Kaspersky Lab" representative said: while you're playing "Minesweeper" or "Solitaire", your infected computer is participating in a virus attack on Pentagon servers.
How I instantly gained respect for our accounting department - turns out they're not screwing around, they're damaging the defense capability of the probable enemy!""",
407963: """Stazz: Moscow will be left without the "Belorusskaya" metro station. Both stations with that name are planned for reconstruction.
Stazz: batka played too far""",
407964: """From the deputy finance director's office come his phone replies:
- She's not here... Not soon... We sent her on maternity leave... With no ulterior motive... And no frontal one... Wise guys, better tell me when your enterprise will pay off its debt to us?.. And you personally, when will you return the money you borrowed before graduation?.. For the tenth year in a row I hear "next year in Tel Aviv", only you're in no hurry to go there... And return the first Fallout... You took it in 2001... That's right.""",
407965: """EvilX: As part of the civic campaign against brothels, in the first two days of work in the Northern administrative district, commissars of the "Nashi" movement discovered about 30 brothels in the district.
Shurik: and made a test purchase:)""",
407966: """xxx: damn, scratched up all my hands
yyy: your hands are from what???""",
407967: """XXXXX: in the passport there's only room left for 3 wives
XXXXX: gotta spend it wisely""",
407968: """xxx: Why throw a microwave that spins but doesn't heat straight in the trash? Can use it as a lamp! Evening coffee! Soft light...
xxx: And put figurines on the turntable: let them spin! Beautiful!!!""",
407969: """xxx: Want an open source bun
yyy: No thanks, I have home edition sandwiches
zzz: Just don't eat in the canteen, everything there needs patching""",
407970: """Watching "animal planet" with my wife, in the frame a mongoose driving a cobra away from its burrow.
Wife - He's probably protecting his eggs.
Me - Did you study anatomy at all?.. though maybe you're right.""",
407971: """xxx: why so long?
yyy: you can't imagine how dumb she is! I yelled at her so much! swore up the whole street. even the gastarbeiters came up to me, asked where to unload the gravel - thought I was the foreman""",
407972: """Trueshechkin:
I just don't get some people. Two junior accountants sit in a cafe chatting about life: "no money, total nightmare". Meanwhile discussing buying a cell phone, which to choose - for 34 grand, the one they like, or for 41, the cooler one. Next, in the "lounge" two "directors" - finance and tech, also chatting about life and also complaining about no money. Finance complains he had to overpay for his Cadillac an extra 200 grand instead of the promised 1.7 mil and his wife's unhappy - they bought her a Mitsu in a cheaper trim, for 1.3 mil. Tech nods along - instead of the traditional Seychelles vacation and US shopping, he had to "drag around Europe".
Ya Petrushko:
And I'm the fool who thought "no money" means you have nothing to eat...""",
407973: """Anasta, 09.09.2010 14:21:40:
if he's got a cold, he doesn't need cold beer
he needs hot mulled wine or something

Lipka, 14:22:08:
I'll ask, but I think I know his choice

Lipka, 09.09.2010 14:44:06:
says instead of the light Kozel he wants the dark one, since he's sick!""",
407974: """xxx: Illness is such a state of the body when, having spilled the last mug of healing drink on the keyboard, you feel sorrier for the drink than for the laptop""",
}
