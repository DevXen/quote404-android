#!/usr/bin/env python3
"""Translate bashim chunk_22 batch 2 (input lines 201-300). ru is read verbatim
from the input file; only en translations are embedded below."""
import json

EN = {
404269: """Sitting in McDonald's today, watching the scene. A little girl about five with her mom, pointing her finger in all directions and yelling:
-Mom, look what I got in my happy meal!
-Mom, look how funny the fry is bent!
-Mom, look, those chairs spin!
At that moment a cleaner comes up and starts collecting trash from the floor.
-Mom, look, a harvester lady!""",
404270: """Ya_Maksimko!: Antoshka
Ya_Maksimko!: Antoshka
Ya_Maksimko!: play the accordion for us!!
Antonio: :]III[:
Antonio: Tra-la-la, fuck""",
404271: """<xxx> in a windowless room where SAP consultants swarm (by the way, since they showed up the toilet is constantly pissed past the bowl), due to the ongoing ventilation system reconstruction, the lights and network were turned off.
<xxx> they're still sitting in there! in a dark room! with no connection to the SAP systems!
<xxx> thinking - maybe hit them with DDT?
<yyy> they'll run under the baseboards, and you'll never get them out at all""",
404272: """xxx: caught Outpost eating 40% CPU. hovered the mouse over it, it got embarrassed and freed it...""",
404273: """Herr Niemand> tonight at my company mice gnawed the scroll wheels off two mice.
Alek Boo> mice don't tolerate competition""",
404274: """Me and my friends agreed to go to a resort base. One of us is very late.
- Vovan, where are you?
- Already on the bus.
- Too bad, we wanted to ask you to bring a pot.
- Ah, no problem, I'll grab one now.""",
404275: """X: found you an admin job, call **-**-**
X: normal job, they say
X: normal money
Y: listen, Dad
Y: that's my old company that laid me off due to budget shortage :D""",
404276: """AlexandRUS
lol, mom just comes in, I have Mozart playing, a violin and piano sonata. Mom: "what's this funeral music?", I switch to Radio Shockman, there's some speed metal on "better?" - "well, livelier at least\"""",
404277: """<Byron> Ubuntu's daughter ran up to Debian and, laughing merrily, kissed him on the
forehead: "Happy birthday, dad!"
Then she looked around the guests sitting at the table with a joyful gaze and
asked in her ringing voice:
- Dad, where's Gentoo, hasn't he come yet?
- No, he's still getting ready.""",
404278: """By reducing hundreds of Roman and Greek gods to one Christian one, humanity carried out an unsurpassed-to-this-day reform in administrative staff reduction.""",
404279: """Girlfriend got a new job:
"signed a pile of non-disclosure papers)))
such an interesting project. I'll tell you when we meet)\"""",
404280: """xxx: just now a chick walked by the office with a phone. Says to someone: "Yesterday they took my plates because I was driving without inspection. Today they took my license because I was driving without plates"
yyy: tomorrow they'll take the car because you were driving without a license
yyy: and the day after they'll take her to the madhouse because she was driving without a car""",
404281: """Alenka: I've been invited for the third time to the group - "Super method how to enlarge breasts to size 3"!
Alenka: like I need it?!
Alenka: I'd rather shrink them!
Alenka: down to a three..""",
404282: """xxx: Glanced at the bottom left corner of the screen, it says - 16:10
xxx: Spent a whole minute wondering why such a format was chosen on a regular 4:3 monitor, until the numbers changed to 16:11, then 16:12...""",
404283: """xxx:
Mom, fresh out of computer literacy courses, decided to punish me by taking away my internet, and, removing the system unit cover while I wasn't home, pulled out the network card, without damaging anything (!!!).

xxx:
That's how we live: a happy mom, a son, and a wifi router in the closet.""",
404284: """Chertenok: Today's my name day!
Brad: Oh? The Church doesn't mind?""",
404285: """xxx: I, Yulka, truly started respecting you when you knocked over a big mug of sweet coffee onto your desk, flooding the report documents, the planner, the keyboard and mouse, and said "Oops".""",
404286: """Wife's reading the little one (2.5 years old) a picture book "A Bag of Apples". In the story a hare found an apple tree, gathered a bag of apples, and while walking home gave them all away to the animals, came back with an empty bag, and the animals brought him what they had...
And so the wife reads:
- They sat down at the table, and on the table there was everything imaginable... (shows the picture)
The little one: There's no meat.""",
404287: """Rambler news feed:
Giant panda gave birth to twins
(and a couple sensations later)
A Tolyatti resident gave birth to quadruplets""",
404288: """Woke up this morning hungover in my clothes at 5 AM, decided to go out on the balcony for a smoke. And my little brother recently gave me a lighter shaped like an F1 hand grenade. So I go out on the balcony, light up and, detachedly tossing the lighter in my hand, stare at the new car in the yard with its top down. Meanwhile a neighbor lady about forty comes out on the next balcony for a smoke too. Looks at me, nervously throws: "Morning, Ilya!" - and quickly retreats back to her flat. At first I don't get it and start examining myself, what's wrong with me. And then I realize I'm dressed in camo pants, a t-shirt with a raised fist wrapped in barbed wire, an army NATO-style cap, curly hair like Che Guevara, a week's stubble on my face, and I'm staring apathetically at an expensive foreign car with a cigarette in my teeth, tossing a grenade...

So here I sit, waiting for my FSB handler to arrive.""",
404289: """x_dec : how's your personal life?
phlowtek : broke up with my girlfriend in winter
phlowtek : it's been fine ever since""",
404290: """about CS:
***
1: what map are we playing?
2: wait, let me talk to my girlfriend, either she'll put out or dd2
1: harsh...
2: I mean dust first!""",
404291: """the boss walks in and says:
- How synchronously you all pressed Alt-F4! )))""",
404292: """Goblinn> Sell, trade or throw it away and build a new deck, considering red and blue are hard-to-combine colors and playing red-blue is hard since one color is fast and the other slow, one runs ahead of the other and laughs, and the other sighs and runs after it. so build e.g. black-blue, very playable, and tell me what tournament type you're building for or is it just for fun?
>arosy> red-blue - incompatible colors? Tell that to CSKA )""",
404293: """Metro incident.
A guy (G) and a girl (D) are riding. The train brakes sharply, the girl falls onto the guy:
D: Oh, f**king hell!
G: Why are you swearing?!
D: It's you techies and mechanics who swear, we economist-translators use expressive vocabulary!""",
404294: """XXX: Once the culturology lecturer, outraged by students' behavior at her lectures, invited the dean to sit in on them.
XXX: The dean behaved like a true student. First sat pretending to be interested, then started playing on his phone, then just fell asleep.
XXX: She had no more questions for us :)""",
404295: """XXX: I cracked my head open=(
YYY: How'd you manage that???
XXX: remember the downpour the other night?
XXX: me and a friend were on bikes near Yugo-Zapadnaya, then hail started, then a wall of rain, we decided to head back, there was some road work, I sped up, only see stars in my glasses (drops reflecting), didn't notice a concrete thingy on the road, and I'm flying thinking what's next, flew a couple meters
XXX: landed seemingly fine, but then the bike falls on me from above =)))""",
404296: """Our town's holding a fair, selling all kinds of junk basically. Walking past a stand with all sorts of electrical stuff, and there's a fan with neon lighting around the blades. So I watch a guy come up and slowly start sticking his finger in there, the salesman, watching this, informs him:
- Interested? It's a special fan for attracting insects!
To which the guy declares:
- No wonder I feel drawn to it...""",
404297: """Mokyс:
what are your favorite positions?

Vredinka:
me in the armchair, fully relaxed, eyes half-closed, breathing even, right hand extended slightly forward... and the mouse going "click-click-click"...""",
404298: """xxx
Today in the "Metro" newspaper I read: "Place your bed or sofa so that during sleep your head isn't pointed at a corner. Electromagnetic waves tend to gather precisely in corners".
xxx
the waves gather in threes and discuss their electromagnetic news, sometimes even have a drink""",
404299: """Shredder: Neighbor went to wash his socks. At half past one at night.
Shredder: Thought I was asleep, washed very quietly. And our sink is piled with dishes :)
Shredder: I was already falling asleep, then something clinked - something small like a fork.
Shredder: The neighbor yells across the whole block: "FUCKING CLINKER!"
Shredder: Stealthy :))""",
404300: """Friends, a couple, had the flu, stayed home a few days. Today the girl writes:

XXXXX:
we recovered in an hour
know how?
a friend brought a new thermometer, turns out ours was lying""",
404301: """from a "bru" forum:
In RF, among holders of a higher-education diploma, a very solid percentage are those whose higher education consists only of the diploma.""",
404302: """- Burned the car ceiling with a cigarette, need URGENT repair...
- URGENT repair is needed when the ceiling is pierced with stilettos, yours can be done in working order.""",
404303: """xxx: What's my password? Write it down: WC3TFTDOTA
yyy: Ugh, a jumble of letters and numbers, how do you even remember it.
xxx: Well let's say: it has a certain meaning for me
zzz: The meaning of life, I'd say.""",
404304: """Oggi:
physiology is physiology, but sooner or later you want to bury your nose in that one's shoulder, and all night be afraid to move so he doesn't wake up. And with a smile listen to his sniffling or snoring. Then smack him in the face with a slipper to make him stop and peacefully sleep next to him again!)""",
404305: """<ultraSLIM> When I stand in the shower - I'm a metalhead too, all in leather and hairy. ТруЪ""",
404306: """- Yesterday felt bad: nauseous, head spinning, my beloved advised taking a cool shower.
- And?
- Well I took a little shower, felt totally fine. I get out all refreshed, linger by the mirror thinking: I'm actually quite something. And start twirling by the mirror picking the sexiest facial expression. Finally, fully in seductress mode, I sidle up to my beloved, sensually run my hand down his back, and he turns around and says so sympathetically:
Natashenka, did you throw up after all?""",
404307: """Zlober>
happened with a girlfriend in fifth grade.
the teacher calls her over and starts pestering her:
- why are you such a loner, why don't you hang out with the other kids, why do you always sit at the last desk, and when all the kids get up and go somewhere together, you're the last to go? do you even like it?
- well yeah... I feel like a shepherd.""",
404308: """\\\\ discussing a girl's vacation at the seaside.
x: that's nothing... I spent half the time walking around in men's underwear... amazing )
y: you walked around in them like shorts? )
x: I just combined the pleasant with the useful - no need to fuss much about putting them on: one, two and done... and didn't have to fend off Dagestanis... apparently girls in men's boxers don't turn them on )""",
404309: """xxx: Near our house one guy had a garage. And the city authorities forced him to tear it down, since it ruins the city view.
xxx: They tore the garage down, put a dumpster in its place...""",
404310: """Blondinka: What, are you offended?
Winnizepu: I never get offended at fools, children and women.
Blondinka: So in your opinion I'm also a fool?!!!
Winnizepu: I don't even know what to answer you now ...""",
404311: """xxx: Forgot my keys the other day, thought I'd come home drunk and wake everyone up
xxx: Nope, at 11 AM nobody was asleep anymore )""",
404312: """on a forum

question: does baby water need to be boiled, or can you just warm it?

answer: I don't boil it, because my daughter drinks from the bathtub and licks the floor
I consider boiling excessive in such conditions""",
404313: """ZoozooZ™: Riding the commuter train home. Dressed, as usual, in torn jeans and a jacket. Met a gop on the way, well actually he met me :-) Said, by the way, that I'm a solid dude :-) We get off at the same stop, walk down from the platform, he points at the neighboring house and says: "Look, fuck, those faggots, building cottages all over!" "Tell me about it" - I say, taking out my key and walking right into that house. )
ZoozooZ™: too bad you didn't see his face
ZoozooZ™: he's probably still standing there stunned )""",
404314: """My girl: I need to wash up, put on makeup and wash the dishes
Me: You can put on makeup and wash up at my place. And wash the dishes too...""",
404315: """<psihos> Max, call me. I'm just too lazy to go get the phone, and if you call, my sister will pick up and bring it to me :)""",
404316: """about scammers :)
Went to a friend's birthday, luckily mom's on a business trip :) at noon 100 rubles arrive on my phone and an SMS from mom "I don't have more on me"... o_O what's this?? But in "can't talk" mode I'm not calling mom. evening, another 500 rubles and the question "how long can you keep chatting?????" o_O
Next day: find 2 more SMS "you're getting on my nerves!!! where are you hanging out and who are you talking to??? no money will be enough for you!!!" o_O
I call back.
Turns out mom got 5 SMS from unknown numbers like "mom, top up this number". but mom reads all SMS superficially, daughter needs money on her phone? top up :) and throws it all to me :) love my inattentive mom! )""",
404317: """It was long ago... The first year of CS at our company was underway. Me and my colleague Gleb are riding in a taxi, the driver - an elderly man, eavesdropping, we're talking about CS.
G - Remember the market in Italy?
Me - Good spot, you can hide behind the tents there.
G - Well I was hiding with a Kalash, and the terrs wounded me, I'm weakly shooting back, but then a couple of ours came up and threw grenades into the corridor where the terrs were sitting... blablabla.... we're talking and slowly pulled up to his building, Gleb says goodbye and walks to the entrance, limping horribly. He had meniscus inflammation then:) The driver turns to me, looks suspiciously and says:
- So young, and already seen so much... GRU? Alpha? What were we doing in Italy?
Explained, after laughing my ass off, the old man swore, both our moods improved)""",
404318: """Irka stands looking out the window (there's a cemetery outside)... Looks and looks, then blurts out:
- It's so quiet over there...""",
404319: """ViSHeNkA: Watched track and field yesterday?
Miracle of Light: Yeah, watched, though not long, since I got a culture shock :)
ViSHeNkA: ???
Miracle of Light: Some big black negro came out for the triple jump, ran up, jumped, and then they called him an American woman...""",
404320: """Tau:
Found where valerian grows. Dug it up. Washed it and hung a whisk of cut roots on the neighbor's fence, whose mutt is dumb and constantly barks.
Now I have a show. Crowds of junkie cats come like zombies, and the dog's going insane. Waiting for the bastard to go hoarse, or for the owner to shoot it""",
404321: """Astrakhan region. A bus stop in an open field. On it an inscription in beautiful font "GOOD LUCK"...""",
404322: """RockyJack: to all the assholes knocking on my door I say I'm a lesbian and they don't write me anymore )
RockyJack: so if girls hit on you - say you're gay )
Atrian: I tell them I'm a programmer
Atrian: and they don't hit on me.""",
404323: """Around 2002 my comp burned out from a power surge. Totally burned, to hell. The floppy drive, modem and memory survived. I couldn't do without a comp then - I went freelance. So, having bought a comp on credit, I decided to equip it with a UPS, even a used one. Met a friend, he was selling one, bought it. Trudging through the metro, UPS in a gym bag. Heavy, the bastard. Transferring at "Kupalovskaya", I put the bag on the ledge (boom!). And it's interesting - a new toy! My hands are itching! I unzip a bit, stick my hand in, turn it on.

- BEEEEEP! BEEEEEP! BEEEEEP!

I raise my eyes and meet the gaze of a cop :-). For like 5 seconds we dumbly stare at each other, then I turn off the UPS, zip the bag and pretend that's how it was. The cop nods contentedly and turns away.

Why am I telling all this? It was somehow calmer back then, yeah...""",
404324: """xxx:
Today on the MKAD I saw:
in the left lane drives a police "six", hazards on, a voice from the loudspeaker:
wee-woo-wee-woo,
weee-woo-weeewooo,
the light bar doesn't work,
clear the road,
weee-woo-wee-woo...

yyy:
yeah, all that was missing was a fighter with a wooden toy gun:
stop the car, I'll shoot!
tratata, tyshch, tyshch,
tratata
)))""",
404325: """Poroh: my cat's like a human
Poroh: put his paw under his head
Poroh: and sleeps
Poroh: cutie
Poroh: the back one....""",
404326: """Lopik: So, how are your girls doing?
xxx: Which? The cat and the guitar?""",
404327: """forum message:
1: have lots of audio cassettes, even licensed ones, hate to throw away, maybe someone needs them? will trade for anything ) offers or wishes?
2: would they do for waste paper? ) I have lots of old and not-so-old newspapers, hate to throw away. can be used in winter during heating outages, insulating the body by wrapping in newspapers, and also lighting fires etc., Offers?""",
404328: """reydan: Oh come on. I just asked what she does. She said she's in consulting.
reydan: In management.
reydan: So I got interested.
reydan: And in the morning it turned out she's a dumb bitch.""",
404329: """By the will of fate I started programming in C# not long ago. And all would be fine, but for some reason I started getting a kick out of a fairly common construct in it -
public static void
Thought long about why. And a week later it dawned on me. It's the stress pattern - exactly like the childhood-familiar KRIBLE-KRABLE-BOOMS!""",
404330: """Creating a report in Crystal Reports, in whose header there's a title "Акт дефектации", it's very hard to resist the urge to make a tiiiiiiiiiny typo.""",
404331: """Newspaper ad:
Trade:
Wedding set (skirt + corset) height 175+, size 46-48 for plumber services (washer won't drain). Tel: (***)***-**-**""",
404332: """Come to the doctor. A man about forty.
He looks at me and in a breaking voice whispers to the nurse:
-Natashenka, we've lived to see it... Thank God...
She covers her mouth with her palm. Eyes like five-kopek coins.
-Really? Is it really him? No mistake?
-It's him
Both stand at attention in front of me. And loudly with pathos in one voice:
-THE ABSCESS OF THE CENTURY!!!

They sit down and already in low voices, as if nothing happened...
- Young man, you have an abscess of the right eyelid.

Aren't they bitches?""",
404333: """Ice Girl: you're a bearded admin )
I+S+L+A+N+D: It's tradition 8-) An admin without a beard isn't real
I+S+L+A+N+D: Just like Santa
Ice Girl: oh, I just realized what Santas do all the rest of the time
Ice Girl: they admin!
I+S+L+A+N+D: now I have to kill you 8-(""",
404334: """Rafus (11:33:51 3/08/2009)
Damn printer. Asks to be refilled. First there was just a message on the printer. Then it displayed a message on my monitor. I ignored it. Now it printed a sheet itself asking to be refilled )

Rafus (11:37:17 3/08/2009)
Tonight it'll probably come in my dream""",
404335: """iexx:
damn, what I love about programming is that the same thing can be done ten different ways.

Obormotik:
and get your ass kicked by senior programmers for 8 of them""",
404336: """beronix:
Entrance. Powerful perforator. Slab+Rebar. Hole for fiber optics.
a guy (resident) came out with the phrase (verbatim) "what the fuck are you doing here?!"
I couldn't hold back and answered "we're grinding away here, like negroes"
the guy muttered "can't argue with that" and left""",
404337: """<Panda> And also, girl Zhenya, puts the number "4" instead of the letter "ch". and that's actually a bad habit
<Joy> why? )
<Panda> because those who put numbers instead of letters will go blind and grow hair on their palms)
<Joy> oh horror )
<Panda> look at your palms, do you see hair already?
<Joy> no )
<Panda> that's because your eyesight has already started deteriorating.""",
404338: """xxx: Tell me honestly, how old are you?
yyy: 27
xxx: good. And where do you work?
yyy: NOWHERE YET
xxx: studying?
yyy: NO
xxx: and what do you do?
yyy: JUST WOKE UP""",
404339: """nebov: My asus p550 also runs IGO fast. so I'm wondering if I won't regret it after switching devices?
2828: my asus now rides in a taxi, otherwise I wouldn't have bought an iphone
nebov: I should give mine to a taxi too, really. How did you sell it? Where did you advertise?
2828: no no no, after the wedding I took a taxi and lost it there""",
404340: """From a World Of Warcraft forum:
When I was a tester, the WoW department head got into it, spent half his work time on it instead of features. The other half on "communicating with players to identify desires"

So he's sitting there, in warcraft. Then the general walks into the office and slowly comes up behind the boss's shoulder. He didn't notice. And stands for like five minutes, watching him play.
The boss turned around, raised his head up, and for like three seconds looks at the general. Looks and looks and says:
I'm here, uh, testing the quest system for our "mmorpg" project. You know, we have a similar system, so I'm looking at theirs, so ours isn't worse, you know.
To which the CEO reasonably remarks: you're testing for a long time, already level 25 )""",
404341: """found an email address in the database
"********@WSNET.RU FOR VALENTINA NIKOLAEVNA\"""",
404342: """xxx
Why so sad?

yyy
ugh, struggling with the internet connection )

yyy
sitting like an idiot, the internet just doesn't work.
I'll go complain to Sasha in the next room, we'll come back, and it'll start working!!!
Sasha leaves, I close the door - doesn't work again... and so 7 times.

yyy
in the end I realized the door is pinching the cable. there are no other reasonable explanations )""",
404343: """From office correspondence:

Mikhail, your programmer's ICQ status is "Writin crappcode". Can we count on the project's successful completion?""",
404344: """From correspondence
xxx: You're such a rare little bitch!
yyy: "Bitchie" sounds like a kindergarten name...""",
404345: """Freelance site.
Client: Assembling a team of writing people - pay for rewrite - 10 rubles/1000 characters without spaces.
Freelancer: For 10 rubles I'm ready to provide texts of this type - длрпжырп3шре тажчражйц. If it suits you - write in PM. Guarantee 100,000 characters a day. Uniqueness 95%+.""",
404346: """xxx: Damn emo...
xxx: How do I get rid of slanted looks in transport now, when you have black hair (because I like it) and red, watery eyes?
xxx: But not because life is shite, but because of violating grinder safety rules!""",
404347: """ted: it all started with the hat, the hat was given to me, so I decided to assemble the full Indiana Jones set. Only need to match jeans and boots to the jacket
ted: and then I can go to Africa, or wherever that Indiana went on expeditions...
MissX: for the grail? )
ted: yep!
MissX: take me then, I'll film it all
ted: filming me? )""",
404348: """"MaNiaC"
At night...
"MaNiaC"
I...
"MaNiaC"
Dance disco! *DANCE*
"MaNiaC"
In the kitchen!
"MaNiaC"
In front of the mirror, in socks on my hands *DANCE*
Raim
you're a pervert)
"MaNiaC"
:-(
"MaNiaC"
I'm offended:-(""",
404349: """nik>> yeah, I have a photographic memory!
malo>> yep, only the flash drive's small, and the resolution isn't great""",
404350: """A buddy just stopped by work, we laughed our asses off. A kid comes home from school, in his diary a note: "Your son behaves inadequately at school - take measures!". To which he wrote back to the teacher: - "Always dreamed of a son, but it's always a DAUGHTER who comes home from school, learn to perceive the world adequately!\"""",
404351: """xxx: bought a car, doesn't go or something
zzz: how much for?
xxx: 18k rub.
zzz: I bought a fridge for that much, doesn't go either...""",
404352: """Otvety.mail.ru
Q: xxx: Suggest a site where I can look at men's mink jackets! Really need!!!
A: yyy: BOURGEOIS!
comment to A: xxx: I'm not buying!!! We just argued with my husband...""",
404353: """J: sanchos is on sick leave
O: what's wrong with him?
J: got doused in bitumen
O: horror! hot?!
J: I don't even know.. try getting doused in cold..""",
404354: """SHE: Romko, I have a problem on the line, can you give me manly advice?
HE: with a deep blowjob you really can throw up
SHE: No, not that ) a girl I know quarreled with her boyfriend, situation: in the morning she realized she can't play World of Warcraft because the credit card her man pays for her with changed: he got another one at the bank or something... anyway, she called him, he dictated all the data to her, she changed it, the payment went through, she's playing.
He asks her - change mine too (he also plays WoW). She refused, saying he can do it himself. He got offended, threw the phone.
Did he really have to get offended? Or who's the fool in this situation?
HE: laughed
HE: now about the problem""",
404355: """XXX: Remember Andryukha's cousin Lyokha?
YYY: Yeah?
XXX: When he graduated uni, his parents got him out of the army.
XXX: Then like 3 more years passed and he had to renew his license.
XXX: He comes to the medical commission, walks into the surgeon's office, the guy stares at his card for a long long time, then asks: And why didn't you serve in the army?
XXX: Lyokha's like, well I sort of have flat feet (he doesn't know for sure himself)
XXX: And the doctor's like -- no, buddy, you didn't serve in the army because according to the papers you have no legs...
YYY: ...And so, did they give the certificate?
XXX: Yeah, Lyokha paid a bit extra and now drives around "without legs"...""",
404356: """From a forum:
I didn't even watch the movie, so as not to spoil the impression from the comments...""",
404357: """<Aleut> at my last job two departments once decided to go out to nature. distributed who buys what. the guy responsible for repellents brought a plug-in raptor. got beaten up and ran to the store.""",
404358: """xxx:
Don't trust sisters!

xxx:
My wife and I went to rent discs. So we picked three movies of various kinds and decided to get the Kama Sutra too. To learn. Didn't show anyone, quietly hiding, watched them. My sisters saw these movies and decided to do us a great favor - took these three CDs back to the store.

xxx:
To which they were told the Kama Sutra was missing...""",
404359: """A wild boar attacking people in China fell victim to an 83-year-old granny, the "Zhongguo Wang" news service reported Thursday.
The unequal fight happened last Sunday in the suburbs of the Chongqing metropolis. A 107-kilogram wild boar jumped out of the bushes at old lady Hu Chengzhen while she was peacefully weeding her garden.
Armed only with a weeding knife, the elderly woman dealt with the wild beast in a matter of minutes.
Granny Hu herself got off with a light fright and a few scratches.

Chuck Norris is probably only afraid of Chinese grannies...""",
404360: """she: wantwantwant!!! it's the Little Black Dress!
she: every girl must have one!!!
he: You've turned my worldview upside down. All my life I thought all girls dream of the Big White Dress.""",
404361: """xxx: My sister was recently visited by her great love: a Frenchman studying at the University of Berlin, whose mom is African-American and professionally paints Orthodox icons.
yyy: Now that's what I call a versatile personality )""",
404362: """xxx: Oh come on, wear Yulka's shoes, you're the same size.
yyy: No, I decided a while ago not to take her things anymore.
xxx: And what was the last thing?
yyy: Her husband.""",
404363: """Long ago, at the dawn of my adminhood.
Sitting at the comp, mom's in the room with me.
My work partner calls me, and we start discussing network problems, traceroutes pings broadcast packets, flood, a hung dslam..
So as not to get distracted from the comp I turned on the speakerphone, naturally my partner lets slip a couple of swear words, to which I warn him, like be careful, I'm on speaker, and mom's right here.
Mom from the other end of the room: "That's the only word I understood!!\"""",
404364: """Male and female psychology peculiarities. My girlfriend and I come to a restaurant, and there, right across from our table, on a shelf stands a vase, unambiguously resembling a briskly standing phallus.
Me right away: - Look, we're not welcome here.
And she: - On the contrary!""",
404365: """xxx: Tell me, anyone familiar with courier work? What are the pitfalls? Thanks.
yyy: If you bring bad news, they'll chop your head off.""",
404366: """he: did you ever cry after we broke up?
she: yes
she: when a log fell on my foot""",
404367: """- In the bus I saw a sign: "The bus stops at stops only on demand. The bus stops on demand only at stops."
- Logical) Was the driver a programmer by chance?)
- Don't know. But the driver was bearded)""",
404368: """<XXX> ...so, she took the best contraceptive with her - her fifteen-year-old brother.""",
}

def main():
    src = open('chunks/chunk_22.jsonl', encoding='utf-8').read().splitlines()
    batch = [json.loads(l) for l in src[200:300]]
    assert len(batch) == 100, len(batch)
    ids = [r['id'] for r in batch]
    assert set(ids) == set(EN), (
        'missing:', sorted(set(ids) - set(EN)),
        'extra:', sorted(set(EN) - set(ids)),
    )
    nl_bad = [r['id'] for r in batch if EN[r['id']].count('\n') != r['text'].count('\n')]
    assert not nl_bad, ('newline mismatch:', nl_bad)
    out = open('out/chunk_22.jsonl', 'a', encoding='utf-8')
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
