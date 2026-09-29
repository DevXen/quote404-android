#!/usr/bin/env python3
"""Translate bashim chunk_13 batch 7 (input lines 701-800). ru is read verbatim
from the input file; only en translations are embedded below."""
import json

EN = {
394853: """Her: will you come over to my place today?
Her: and I'll cook something tasty)
Her: or at least something edible))
Her: maybe…
Her: if I find something to make it from…
Her: which is impossible in itself…
Her: in short, if you're coming, bring something to eat…
Her: tasty stuff))
Her: and lots of it, my brother was going to stop by)
Her: with his girlfriend
Her: so when should I expect you?""",
394855: """Daredevil: That's it! I challenge you to a duel!!!
[Wishmaster]: Which weapon do you choose? Rapier, pistol?
Daredevil: rapier
[Wishmaster]: Good, I choose the pistol)))""",
394857: """<wasisualij> Israel has unveiled a new communications development from the Tjat company: a modern cell phone whose retail price will not exceed 25 dollars, reports Ynet.
<IgorNik> wasisualij: only it's a bit scratched up, without a charger, and no documents...""",
394858: """* Daemon poked wd with a stick
<wd> ya
<Daemon> and what'll happen to perl if you stuff 2 gigs of data into a hash and make perl sort it?
<wd> it'll sort
<ghoulWork> unexpected""",
394859: """UUU: listen, what's the deal with Denis's knife? everyone's smirking, nobody explains
XXX: aaah, it's a trophy)
XXX: so, he was once taking out the trash - we were smoking on the staircase, and on the upper landing a gopnik latched onto him and started threatening him with a knife
XXX: heh-heh, now I'll try to describe it more literarily, it's worth it))
XXX: I still remember Denis's predatory grin, the phrase "my kung fu is stronger than yours", the bucket of sewage on the gopnik's head and a juicy kick to his balls, as well as Denis himself jumping around the guy clutching his dignity, loudly chanting "freedom to the diclonius, Darth Vader shall not pass!"
UUU: that's brutal!
XXX: yeah)) though the most brutal part was when Denis had to collect that sewage from the staircase landing...""",
394860: """Alve
They just said a scary phrase on National Geographic

 Alve
"Up in the far north, the pesets makes itself felt very fast\"""",
394861: """Orcinus Orca: the boss tried to install Zope as a service. In the morning he storms into the office, eyes red, purple circles under them. He scared off all our girls with a single phrase: "I was digging around in the zopa all night and found nothing worthwhile\".""",
394862: """KirKir: our HR department is losing its mind, they're all trying to get onto Odnoklassniki and ZhZh and they get shown a picture "Work, slaves" and a link to the company website :)""",
394863: """FallenGameR: mom came in, said the tea was ready. then added that Rastorguev's kidneys had failed. and started telling me about some granny who had 3 kidneys and wanted to donate one, because she loved the singer so much. but then it turned out she had 2 kidneys...
FallenGameR: fuck, why the hell do I need to know all this?!
FallenGameR: I won't be able to sleep now!
Atos: and me? )""",
394864: """>> ATTENTION: user TITS is joining us. Let's welcome them!
>> : user TITS receives a warning from moderator MILADY. Reason: YOU HAVE ONE MINUTE TO CHANGE YOUR NICK... IT'S INDECENT
>> ATTENTION: user TITS says goodbye and leaves.
>> ATTENTION: user BOOBS is joining us. Let's welcome them!""",
394865: """Lyokha
mom's temp is 45, is that normal?
 Serg
You scared the hell out of me at first...
 Serg
It's normal.""",
394866: """1: Imagine, my dad at work has an air conditioner with a natural-wood remote!))
2: Yeah right, you're pulling my leg! Damn bourgeois..
1: No way, I saw it myself today! His regular remote's batteries died, he's too lazy to buy new ones. So for the second day now, as he leaves work, he grabs some long stick and starts poking at the air conditioner, trying to hit the power button!))""",
394867: """Trinidad
How did your trip to the draft office go? And what did you eat before it? 
HellRider
ate nothing.
everything's fucking awesome. worked it out. they issued the military ID, wrote in the specialty pilot-saboteur, and said we won't meet again until WW3 starts.""",
394868: """1> Imagine, someone's breaking down the door to my room and yelling they'll kill me :(
2> And why were you ogling Svetka? )))
1> And how do you know???
2> Dude, don't keep your private porn in a shared folder :) By the way, reinforce the door, her husband seems to have grabbed a hammer""",
394869: """1
hi

1
what else is there 
besides QIP Rambler ICQ and Miranda

 2
hey

 2
there's also booze and chicks

 2
 porn, weed and stuff

 2
and it's all way more interesting than QIP Rambler ICQ and Miranda))))""",
394870: """Milog
maybe get a turtle?
 Milog 
it won't run away at least)))
 Milog 
or a talking parrot
 irena
no, no turtles, no parrots, all you get from them is shit
 irena
only a kitten
 Milog 
 and from a kitten we'll be getting milk and eggs and meat, right?))))))))))
 irena
it's for the soul
 Milog 
do you know how soulful turtles are?)))
 irena 
no
 Milog 
soulful as fuck
 Milog 
you come home, and it crawls out from under the radiator and crawls to you, crawls.... wags its little tail, rejoices )))""",
394871: """And who am I after they send me explanatory notes like this: 
A splainatory note.
I, XXXXXXXX, came to work as required.
Turned on some banking programs (ICQ, for example)
wiped the little table and went for a smoke.
The fact that I checked in later,
doesn't mean I came in later.
But pleeze furgive me
and do not ponish me.
13.02.08""",
394872: """xxx: blya... I'm fed up with 15-year-old dumbasses...

xxx: here I am cramming cold ocean films, and they're nagging my brain...

yyy: tell them about cold ocean films too. or better, about something more erotic - like, the movement of tectonic plates....

xxx: they slowly crumple each other...

yyy: yeah, one enters the other... and the waves roll in...

yyy: and then Indonesia gets fucked, blyat.""",
394873: """One dude wrote this in Fortran:
a=1785 ! LVB
and left for a long time
people spent ages figuring out what this evil comment means
and it's the only one in the prog
the author's answer is genius:
I remembered that this is Ludwig Van Beethoven's birth date, and thought it was funny""",
394874: """XXX
do you know the Soviet 30s board game "Endure, pioneer"?
OOO
no
XXX
my grandpa told me
XXX
so it was sold in cardboard boxes like that with happy pioneers 
ХХХ
there's a small Van de Graaff generator and two wires. The driver grabs the wires with his hands, and the second driver cranks the machine.. then they switch, whoever lasts longer wins.
ООО
they should make more of those nowadays""",
394875: """Today I come to a job interview, we're sitting there filling out a questionnaire with an unfamiliar dude.. Filled them out, go to the department head.
He (carefully studying my questionnaire): Gorbachev, so... (smiling meaningfully))..)
After chatting with me for a couple minutes, he takes the neighbor's questionnaire, and with bulging eyes, in great surprise... Chernomyrdin...""",
394876: """xxx(06:20:45): I need to call mom, tell her I won't be sleeping at home
yyy(06:20:55): I think she already knows.
xxx(06:21:10): How?? She's asleep!!!!""",
394877: """There are girls who have lips on their face. Others have a mouth, or a tiny mouth. There are even girls who have veritable lips in the lofty sense. They converse with them so pleasantly.

But Svetka, fuck, has an oral orifice in that spot.""",
394879: """Sometimes you just want to save the game and make Zhirinovsky president..""",
394880: """Vika: Things are moving so fast with us - today I've already fallen in love with you. What'll happen tomorrow? sex? In 3 days a wedding already, and in five a baby???

Me: of course it's a fucking great plan, but if you give birth to a baby in five days - it won't be mine.""",
394881: """1: privet. ya iz-pod linux. napishi chto-nibyd'... dlya testa
2: flour, eggs

(c) chie.f""",
394882: """L:
and you're also a writer. you've got fangirls. ladies' favorite. winner of literary contests. got a haircut - now you're handsome too. they love you at the institute. you know your art. your friends are cool, leveled up.
fuck the impotence""",
394884: """unrealm: Kolyan, tell me honestly, what did you get in physics at school?
Nikolai: shitty))) 3s, sometimes 4s. Why?
unrealm: Remember the day before yesterday you gave Lena a cool little pouch for small stuff for St. Valentine's Day, she immediately repurposed it for her phone...
unrealm: So, what I'm getting at... Yesterday she complained to me all day that nobody calls her, I told her it's probably something with the phone, I called it, turned out everything's fine, and today I met her and took a closer look at your little gift... The little case, motherfucker, is woven from thin-thin wire!!! 
Nikolai: and?
unrealm: Off you march, dog, go learn physics!""",
394885: """* index01d wonders where to score 60000 rub
* Alex01d has only 2.5k in cash
* index01d wonders where to score 300 alexes""",
394886: """alex 
In what year did Windows 95 come out?
filin_groove
youwouldntbelieveit""",
394887: """xxx: virtual sex does have its advantages...
xxx: while you're fucking you can go put the kettle on and eat a couple of pies""",
394888: """But there have been changes in recent years - earlier support just said "we'll call you back", and now "tell us your phone number - we'll call you back\"""",
394889: """Hedgehog: Going through a new programmer's code. stumble on a line:
"Kolichestwokeshirovannihzayavleniyvlevomochke=Kolichestwokeshirovannihzayavleniyvlevomochke+i;"
laughed, explained that it reads badly, make it pretty, I say.
after some time I look again:
"Kolichestwokeshirovannihzayavleniyvlevomochke+=i;"
... An optimizer, damn.""",
394890: """1: Hey
Dima, do you know people who help folks with computer questions?
What's the going rate now?!
2: >What's the going rate now?!
I'm not in the loop, haven't stocked up in a while, but they say a fresh batch recently came in from KZ, took it through an offshore in Meleuz
2: and the conspiracy with the computer question - that was a smart move of yours""",
394891: """Got the washing machine back from repair. Two movers barely dragged it from the elevator to the bathroom. Puffed, swore, sweat poured off them. They say it's unrealistically heavy.
When they left, it turned out the service had forgotten to drain the water from the machine...""",
394892: """Nymphetamine (14/02/2008)
so explain, why is it that when everyone walks their chicks, we're sitting in front of the comp....?

 Nymphetamine (14/02/2008)
I feel like I'm about to drag the sys block to the movies.......... things are serious between us....""",
394893: """XXX (04:34 PM) : 
right now at work it's hilarious... a price list (in Excel) for audiobooks came in, and in the "format" column some shrimp did autofill. so, our audiobooks will be in mp3, mp4, mp5, mp6, mp7, mp8, mp9, mp10, mp11 and mp12 formats""",
394894: """1: Well the idiot's dream came true)
2: spill it
1: on Saturday it's my son's birthday, bought him that very helicopter))
2: tested it on the chandelier?)))
1: yep, can't reach(( but it trimmed the flowers fucking nicely""",
394897: """DOM2 show participants called THE VERY BEST FILM tasteless""",
394898: """Alukard
Hi! Happy Saint Master day

Alukard
damn!

Alukard
*Valentine's))""",
394899: """<ToxicMan> All very cultured. I start having sex with my girlfriend. And then she blurts out: "This device can work faster". I can't fuck and laugh at the same time ((""",
394900: """art: I figured out how Open Source makes money, after I stumbled in the sources right above the line that threw the compiler error on /*Place for your advertisement*/""",
394903: """Walking and reading on the wall: "Medvedev!". Strange, earlier they wrote "DICK" everywhere...""",
394904: """I'm German. A manager by profession. Living in Moscow for a year. And I speak Russian quite well.
But I've been reading BASH.org for a month.
And I've started having problems with language grammar…
Gentlemen administrators, fuckbitches, your mission is accomplished.
(c) Khimera""",
394905: """xxx> damn, our country is described in one phrase
xxx> the antifreeze froze""",
394906: """khatarnak: You bought sausage, you're walking home and a pack of dogs attacked you??
khatarnak: Defend your doctoral thesis!!""",
394907: """xxx: Heh, my whole family's already in stupor...
yyy: ?
xxx: Yeah, my stepdad suddenly got interested in where the washcloth disappeared to from the garage...
xxx: The answer "it broke" hung his brain well and properly.
yyy: ??? What do you mean?
xxx: Already told mom, now both are extracting what actually happened...
yyy: Well?
xxx: Damn, that was last(!!!) winter. I dropped it, frozen. Wort and broke it into 4 pieces...
yyy: Yyy... Pc. Strange they only realized now.
xxx: Exactly. I found a helium canister here. I wonder what they'll say about the flown-away bucket? XD""",
394909: """p_s_motorchikom: did you figure out what the letter "p" in my nick means?)
IE: no)
IE: ah! got it!
The Owner informs that IE is leaving us
The Owner informs that kh_s_garmoshkoy is joining us
kh_s_garmoshkoy: it's IE. did I guess right about p_s_motorchikom?
p_s_motorchikom: dummy( a ghost with a little motor""",
394910: """A friend (programmer) tells his nightmare:
-I dream I'm running away from a cat... a big scary one... and in my hands I have a kitten, turns out she's chasing after it... so I'm thinking - I need to make the cat stop thinking it's a kitten... and you know WHAT I did?
-threw it away???
-nope! I opened the kitten's properties and renamed it!!! and I distinctly remember the phrase: Do you really want to rename "Kitten"....""",
394911: """xxx: you're already a big girl
xxx:and you ride at night, with three dudes, in a supermarket cart""",
394913: """Saschalein
The lecturer really burned today at the university:
"Per safety regulations, in case of fire the teacher must exit last. So I really ask you, if something happens, to exit quickly so I can survive too.\"""",
394914: """On the torrent tracker:

Mcline: Remove the publisher info. About the publisher, not the developer. Otherwise the torrent may get killed

kilkus: What will change?

deadlymercury: for 1C to prove it's their disc - they'll have to download it, as a result they'll commit a criminal act - and they can be sued for downloading a pirated copy of 1C products 
laws must be respected!""",
394915: """1: We're gonna get a mafynka!
2: a kaku?
1: SsangYong Actyon
1: Diesel with automatic
2: got it... you could've just said "yes\"""",
394916: """Her: I loved you!!!))))
Him: .....don't even know what exactly scares me in this statement - the past tense, the masculine gender, or the Georgian accent....""",
394917: """1: And what's that thingamajig in the photo?
2: It's a sextant. And, anticipating your question, no, it has nothing to do with sex.""",
394918: """When I told the girl that in this dress she looks like a phallus with arms, the last thing I expected was that she'd hide her arms....""",
394919: "Andrushko\\Golub mode \nyeah he's rich, but there's ONE thing you can't buy for any money\n\nJollyLolly\nAnd what's that?\n\nAndrushko\\Golub mode \nA DINOSAUR!",
394920: """In the store I'm standing with a cart in the checkout line, I look, and everyone around is staring at me.
I lower my eyes, and in my hands is a cart full of bread with a big yellow sign on a stick stuck in it saying "Super deal! Bread 10 rubles!"
I told everyone that I just really love bread and went off to look for my cart to everyone's laughter.""",
394921: """<Majus> Ffs.. Sitting here going through someone else's code. 
<Majus> What could be worse. 
<const> ...finding out it's your code""",
394922: """gluck: Admins have their notion of work turned upside down. If an admin has no work, he gets a bonus. If there's so much work that he's working around the clock, he not only doesn't get a bonus, he gets his ass kicked too.""",
394923: """Kau-chuk: Stas, help me set up the Donkey for downloads
Sts: Sergey, I'm in the middle of delivering a project, so I'm experiencing a certain shortage of time resources.
Kau-chuk: heeelp meeee!!!
Sts: fuck, I'm explaining to you that I don't have time to finish the project, the boss is roaring and every 5 minutes runs up to me with his dick out and fucks me so hard that molten hot vaseline flies across the walls!!!
Kau-chuk: Dude, what's with the yelling, you in a bad mood or something?!
Sts: fuck off, get lost with your donkey and let it fuck you, you're a moron, gallop off to freedom into the pampas and, having lined up in a herd with the same dumbasses, cleaving the savanna grass with your mighty chest and screaming I AM A STUPID FUCKING MORON, fuck off into the sunset, and leave me alone!!!!!!!!
Kau-chuk: why are you yelling... you could've just said you can't
Sts: bitch... shoot yourself in the head with a recoilless rifle""",
394924: """From a phone review
Having removed the cover, a microSDHC memory card slot appears on the right edge. Installing or removing the card is very convenient, but you need to take it out carefully - the latch showed one of the best results in memory card shooting: 3 meters and 17 centimeters, that's no joke.""",
394925: """Fiver: well, you got damn lucky :-) The irony of fate, really. Catching an infection from a girl surnamed Kasperskaya...""",
394926: """Serj
God, how stupid are you. 
Dench
Blasphemer.""",
394927: """rest> As a kid, a friend assured me that at night an unclean spirit possessed his older sister.
It later turned out she was just masturbating.""",
394928: """Yesterday I watched an interesting scene.
in the yard three guys were arguing about something, so, two of them are standing there, cursing. Raising their voices higher and higher. You start hearing them yelling something about linyaga.
In the end one of them smacked the other straight in the jaw, and added a kick too. And yells
- Well, asshole of the 80th level? Got nothing to answer with in real life, huh?""",
394929: """<karakhan> Hurraaay! I found another pervert who eats cheese with strawberries!!!
<karakhan> But he's a rat...
<karakhan> And from a cartoon, too...""",
394930: """yozhkin kot: JULL, you're an interesting person, how do you manage to stay late at work while being on sick leave?""",
394931: """<Dr.Alf> when are you returning my 500 rub, you dickhead?
<SwazilenDenok> lyokha! hi! I just picked up a chick on ICQ for sex ))) meeting her tomorrow by the screen. 
<SwazilenDenok> blonde, size 3 tits, 19 years old, hot, knows about AC/DC, almost beat me at q3 and wants me )))
<SwazilenDenok> going to get condoms now, so we won't see each other tomorrow... I'll pay you back later ))
<Dr.Alf> see you... ^-^ hi from Sklo4nica
<SwazilenDenok> how do you know? o_O
<Dr.Alf> I just know ))) bring 500r with you and for beer. you can skip the condoms *ROFL*""",
394932: """zhenshchina-koshka:
And also a colleague from America is bringing him some computer device for just half price. And he's happy about it like a boy with a bicycle!

 zhenshchina-koshka:
that's so true, a man differs from a boy only by the size of his toys ))

 Steel_Cat:
Oh, these women... 
All you think about is SIZE ))))
It's not the size, it's the PRICE of the toys that differs !!! :)))""",
394933: """Dragon 
Try sending an ICMP request to any node in the range from me to you

 Dragon 
And you'll see nothing will come of it...

 Poltergaste 
Yep, nothing came of it

 Dragon 
See, I was right.

 Poltergaste 
Yeah, I just don't know how to do it...""",
394934: """/23:26/ Zhilkin: so what's up with him? was he kidnapped?
/23:27/ Markiz : dunno. vanished suddenly
/23:28/ Zhilkin: and when did he promise to be back?
/23:28/ Markiz : sec
/23:28/ Markiz: "22:48  I'll go get a pill for 10 minutes"
/23:30/ Zhilkin: he picked the red one, apparently""",
394935: """1. This is nothing. You know what Vista writes if it went into hibernation because the battery died?

2. what?

1. A prompt at boot: "Continue restoring slaves". Fuck, I suspected Microsoft didn't think highly of me...""",
394937: """vovka:
modem go walkies?
 me:
modem beep-beep connect internet""",
394938: """jekfat: Today I was making the shooting schedule. Among other things I wrote: "February 30 - shooting six programs". Sent it out to the whole team. The first to respond was the administrator: "The partner refuses to rent out the pavilion on February 30. Doesn't explain why\".""",
394940: """the drilling foreman calls me, asks me to open a document. They were sent a file in pdf format (the document is extremely important and urgent), there's no Acrobat on the computer, installing anything is impossible, permissions are cut to the bone, I'm explaining to the guy:

Me: I won't be able to open it for you, you don't have Acrobat Reader
Foreman: Yeah, no acrobats here, we've got nothing but fucking clowns""",
394941: """Blue Dragon: All your jokes with a flash drive on the back of your head under your hair are bullshit. I come to the doctor for a medical exam, they sit me in a chair, put a special contact mesh on my head, and leave to another room.
Blue Dragon: then there's a "new device detected" sound and the doctor's quiet voice - "so much junk...\"""",
394944: """doze
fuck... I hate programs for linux.

 doze
you ask it, make me a table

 doze
it gives you a 1x1m piece of plywood.

 doze
You ask what is this?
- A table
- And where are the legs? and why the splinters?
- Uh... well install four leg-plugins, a varnished-coating plugin, and to hold it all together another dozen nail-plugins and a superglue plugin

 doze
And still, if you then put a plate of borscht on it, it turns into a hellish mix of Himalayan goat milk, Brazilian cucumbers, English pudding, sushi and lyulya-kebab, because Cyrillic isn't fucking supported.""",
394945: """XXX
was at two job interviews

YYY
how'd it go

XXX
I don't want one place. the other one - me )""",
394946: """<xren> I'm angry.
<Exp3rT> not angry, but a burnt piece of wood:)""",
394948: """Danuuk: oh teacher! how was I wrong?! you have grasped the boundaries of dao, you know the secret of the one-hand clap and you know what sound a falling tree makes in the forest when no one sees it. enlighten me oh teacher.
Danuuk: teacher?
duckhawk: a tree falls like this - huyak
Danuuk: I think it's pizdyk!
duckhawk: don't argue with your elders!
Phistan: And I think a tree falls like this - yeblys'! 
Though... I think it depends on the species of the tree. Could be huyak and yeblys' and pizdyk...
Phistan: But that's when no one sees. Otherwise the tree falls with the sound of BLYYYYYAD.""",
394949: """dima-kancerv:Why do you have your status set to "unavailable"?
Suicide Blonde: that's just how I am)""",
394950: """<anclbob> how do I block the range 192.168.0.0-192.168.254.254 on freeBSD?
<anclbob> I made a file with 65 thousand lines like these:
ifconfig_fxp0_alias0="inet 192.168.0.0 netmask 255.255.255.255" 
..... 
ifconfig_fxp0_alias65434="inet 192.168.254.254 netmask 255.255.255.255" 
<anclbob> put it in rc.conf, but FreeBSD booted for 20 minutes and only got to the 192 subnet. 
<anclbob> How do I solve this problem so that unwanted users can't get into the network no matter what?""",
394951: """maryro: I forgot to tell you about today's dialogue between two ladies aged 75 and 80. The first one is a teacher at MGIMO, the second one holds the French order "for merits" II degree.
- My grandson dresses, as they say nowadays.. ah yes, like a loser.
- A ce qu'il parait, c'est derived from "lokh"?
- Adorable! How well you feel the word, ma chere! At first I thought it was somehow connected with a horse.
- You're just not in the modern context.""",
394952: """A conversation on a forum:
Subscriber- Come on, do something already! Your equipment in our building sits behind such a flimsy door.. That door has been nearly ripped off by thugs more than once.. Just yesterday I chased off some...
Tech support rep- Dear sir.. Those were our employees...""",
394953: """<Shaman SE> Worked in an office where everyone played MMORPG all day long at the same time. A new secretary came in. Damn. Her first month of work wasn't easy. Many life values got reevaluated. The most important questions for her became:
> Why do programmers from time to time ask to bless each other?
> Why is everyone going to some "dungeon" again in the evening, and she's not invited?
> Why is a well-earning designer "again trading weed all night to buy boots at the market"?
> Why do the guys keep threatening to "fuck each other even with speed on"?
> How to react to the phrase of an upset dude "Fuck, I'm out of nail polish again!\"""",
394954: """skotic
an authorization request came to my ICQ with this text: 
"Tell me please? I want to get acquainted with you and discuss? Will you agree? For I know not who this is or what they want. How much is a mighty enlargement to 25 cm".
What do you think, are these elves?

besisland
Some dialect of Sindarin, obviously. You can vaguely make out the roots "enlarge", "mighty", "25 cm".""",
394955: """There's a promo going on in the city.. promoters ride public transport with empty (you can see through the handle holes) system-unit boxes and talk about what a good computer company it is.. so anyway two promoter girls get on a trolleybus and start their blah blah blah.. next to them stands a guy carefully examining them and (since he's wearing headphones) loudly declares to his friend: -I wonder if this girl knows there's no computer in there..
 the trolleybus cried""",
394957: """X: yeah.. I've got a fucked-up schedule and... first, third and fifth periods on Saturday.. bingo fuck""",
394959: """Leo
Artyom

 bobyor412
huh?

 Leo
so my sister brought me a medicinal patch (old eastern recipes), well I've got a pinched nerve in my spine. Stuck it on - helps well. So....

 Leo
WHY DON'T YOU BUY ONE YOURSELF AND PLASTER YOUR FUCKING WHOLE HEAD WITH IT???""",
394960: """her: why did you hang up?
him: I hung up the phone because if I hadn't hung up the phone, I'd have dumped you!""",
394961: """Having read bash, I've come to understand that one half of the country's population is going to vote for Zhirinovsky for fun, and the other half won't go to the elections at all, because it's clear anyway that Medvedev will win...""",
394962: """[+] Ksorchag: was at kofetun, then at dobry el
[+] Ksorchag: there were sitting hellish IT guys
[+] Ksorchag: at least 20 people
Lord_niteN: how'd you figure
[+] Ksorchag: you can tell by looking at them 
[+] Ksorchag: someone there had a birthday, he got: a plunger, an enema, some ikebana made of burdocks, a megaphone they used to place orders into, and other crap""",
394964: """At the end of 2007 the band "Kukryniksy" released a new album.. which is called "XXX" ... which makes it much harder to find on the net..""",
394965: """<Gorthauer> I honestly don't quite understand the service in WinMediaPlayer that lets you buy from an online store the track that's currently playing %)
<Antikiller> Well like maybe your conscience will wake up)""",
394966: """<Mod1koM> got nothing better to do in the evening than sit at the comp and test some stuff
<Civil> Mod1koM, if it weren't for people like us, you'd be eating raw meat in a cave right now""",
394967: """A friend writes:

English has completely eaten my brain, do you know what a "pakpaka" is? (it's spelled Packpacka)
I was going through a box of the kid's drawing supplies, found a Tom & Jerry album, on the cover there's the cat with the mouse, and above them this "pakpaka".
I sat for a minute, dumbfounded, trying to make sense of it.
My brain refused to accept, in the context of American cartoons, the simple Russian word "raskraska" [coloring book]!""",
394970: """>> witchlord:
raised a virtual machine with Windows under Slackware in 3D-desktop. hilarious - a transparent twitching Windows in a window
« k0rn:
did you stop taking the pills your psychiatrist prescribed?""",
394971: """An enthusiast from Germany built a full-size model of the "formula" McLaren-Mercedes MP4/14 race car out of matches.

It took six years, 956 thousand matches and 1686 tubes of glue to create, reports the Jalopnik website

The main thing in this business is the right glue, the happy creator thoughtfully notes...""",
394972: """Just Max (13:19:28 19/02/2008)
Fucking security at the kinap is hilarious. on Saturday I came there wasted as hell. took vodka, then asked the bartender for a sheet of paper with a pen and wrote "I am the bartender, I give beer". Five minutes later a security guard came up and gave me a sheet that said: "I am the security guard, I give beatdowns". I was rolling on the floor))))""",
}

def main():
    src = open('chunks/chunk_13.jsonl', encoding='utf-8').read().splitlines()
    batch = [json.loads(l) for l in src[700:800]]
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
