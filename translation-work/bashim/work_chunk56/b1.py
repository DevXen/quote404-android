#!/usr/bin/env python3
"""Translate bashim chunk_56 batch 1 (input lines 1-100)."""
import json

EN = {
438137: """xxx:
The internet is down at work this morning.
The boss, turning on wi-fi on his smartphone:
Ok, Google, the internet isn't working.
And silence...
Well, the internet isn't working""",
438138: """xxx: The kid managed to eat half the soup with a fork. Because we have a caring mother and an attentive daughter.""",
438139: """Husband:
- interactive TV is when you can argue with Winnie the Pooh?""",
438140: """My nephews got a female Jack Russell. Well, this little lady settled right in and started running around the apartment looking for someone to play with. Found. The cat. The cat is dignified, strutting royally across the whole floor. His royal balls swaying importantly from side to side. Until THIS showed up. Now the cat lives on top of the wardrobes like a ninja, because the little lady got extremely interested in that particular part of the cat's anatomy. She chases him and grabs his pompoms. I mean, come on, he's the one teasing and not playing!""",
438141: """In the store I'm buying apples, a tall girl in high heels walks by.
Saleswoman:
- Ugh, so tall, and in heels!
Me:
- Actually, discussing other people's looks isn't polite.
Saleswoman:
- And is it polite to wear heels at that height? I feel like a hedgehog next to her.""",
438142: """This year my aunt didn't give her sons (all of them well past twenty) underwear for New Year's for the first time... And on January 1st she got a desperate many-voiced wail: "Mom! Where are our undies? What are we supposed to do now, wear the old ones all year?!!\"""",
438143: """I'm on my second maternity leave, which ends in a few days. Because of that I'm quitting. My mother-in-law calls, asks how I'm doing, what's new.
Me: all good, just quit and went on a trip.
MIL: O_o where from???
It was the 5th year of maternity leave...""",
438144: """They're looking for a programmer on the forum.
xxx: What exactly is the program supposed to do?
yyy: a TV guide for the state broadcaster. to write a TV guide program they need a TV guide programmer.
xxx: Why though? I actually tried to wrap my head around what was written. My brain raised a white flag and tried to leave my skull! You guys are evil...""",
438145: """ххх: Sooner or later "that person" appears in everyone's life. The one and only. The kind of person because of whom one day you'll smile broadly and say the three magic words:
Me.
Need.
A pitchfork.""",
438146: """yyy: don't you regret sleeping through a third of your life?
хxх: nah, I regret I can't sleep through all of it.""",
438147: """xxx: I love chanson because it's music about real life
yyy: it's not about real life, it's "stole, drank - to prison. romance!"
xxx: well! real life!
zzz: well, to each their own idea of life
yyy: we, for instance, like death metal""",
438148: """Discussing the new flat camera
ххх: As an option: a GoPro camera as a forehead sticker :)
ууу: a new word in the porn industry""",
438149: """At work Valera - a guy around 50 - is telling tech support about a broken printer:
- Printer, yes. Doesn't print. Brand - brothers.
In my head: "BROTHER, VALERA, BROTHER! BROTHERS IS NOT A PRINTER AT ALL!!\"""",
438150: """ххх: It's really bad to have a red handkerchief, especially if you get nosebleeds sometimes. You think you just have a runny nose and you pick your nose or blow it without a second thought. You wonder why people are looking at you like that. And then you see yourself in the mirror... Horror movies can't compete.""",
438151: """xxx: support
Girl: "one minute, I'll connect you to a specialist"
and in a whisper: "Lyosha, Lyosha, come here\"""",
438152: """xxx: I've figured it out too! Figured out who our telemarketers are! They're hobbits! During a workday they have up to 5 meals: breakfast, second breakfast, an 11 o'clock snack, lunch and tea. I think when they get home they have dinner and second dinner too :D
yyy: But then, if push comes to shove, you know who'll carry the ring to Mordor.""",
438153: """xxx> I'm surprised when you talk about pleasant tactile sensations from me though sometimes... I think, was your previous girlfriend rough or something... )))
yyy> yeah, those scaly-bumpy ladies from my home planet bored me quickly ;)""",
438154: """wife: this morning I woke up and thought, did it happen or did I dream it :-)
wife: then I looked in the bathroom mirror - judging by my smug face - it happened :-)""",
438155: """I learned there's no justice in the world when I was about ten.
Mom once cooked some epically inedible slop instead of soup: apparently something in the ingredients was spoiled. With a scandal, despite all my tears and assurances that it was poison, she made me eat a full plate, and force-fed half a portion to my little brother with a spoon. And so, the kids finally fed, she pours herself a plate, tries it... and with the words "what the hell is this crap," dumps her plate and the whole rest of the pot down the toilet! There's no justice in life, and the soup is proof.""",
438156: """I work with a Volodya. Despite being fairly young - about 30, I think - he already has a lot of gray hair, whether it's genetics or his life's been hard. Since the name is, to put it mildly, common, to avoid confusing him with five namesakes, behind his back they call him "Gray Volodya".

We're discussing the holiday duty schedule. A shift is two people, and the question came up of who to pair with one very nasty and scandalous old guy. A suggestion is voiced:

- Let's go with Gray. He's already gray, what does he care...""",
438157: """Walking through the city center. In some cafe there's an ad on the window: "Business lunch", and in parentheses "shawarma".""",
438158: """ххх: this is really something... like an ad for a February 23rd getaway for real men

Interactive exhibition of small arms of the Russian and Russian Imperial Army, the Soviet Army with the option to simulate firing the exhibited samples;

ууу: well, yeah, whatever. Grabbed a rifle and like a real man: "psh! psh! Tra-ta-ta-ta-ta!\"""",
438159: """Dima: what's the difference between a setter and a constructor?
Victor V: a setter is a breed of dog, and a constructor is a toy like that :)""",
438160: """xxx: Black bugs showed up in the kitchen
xxx: We've tried everything with them, but there are more and more of them!
xxx: I feel like soon these bastards will chew the word "bugs" into our apartment documents and evict us the hell out of here""",
438161: """XSLT debugging works crooked in Visual Studio, it's infuriating. I turn to my colleague:
Me: If I smash the monitor, how long until a new one arrives?
C: An employee?""",
438162: """News: 13,698 kilograms of milk in 305 days of lactation – an absolute record for Altai Krai, delivered by a black-and-white cow named Elza from the Prigorodnoye training farm.
xxx: Elza's Ocean""",
438163: """Best_dno: Was lying down reading an article on Lifehacker about how to improve your sex life, 7 rules.
The phone froze, I reloaded the page and opened an article on how to pump up your ABS! >_< O_o""",
438164: """xxx: Read on anekdot.ru: "Feed a girl all you want, you'll still have to get her drunk." Thought about it a lot. What would you solder her to - the motherboard, the power supply, or what?""",
438165: """Sharing impressions of reading Braudel's "The Structures of Everyday Life"
xxx: They hewed furniture with an axe; the plane only started playing its role in Northern Europe in the 13th century.
yyy: Yeah, and they baked bread once a month.
yyy: And in some regions they chopped it with an axe
yyy: That's why the plane showed up so late - the axe had three functions (tool, weapon, and cutlery)
zzz: How come three?! What about axe porridge?""",
438166: """xxx: Oh, if they were all stupid, they wouldn't be able to do so much harm.
yyy: Well, one goat in the garden is enough to do a lot of harm, but that's hardly proof of its intelligence :)""",
438167: """[from the corporate chat]

xxx: good morning. builders have been banging away here for an hour already
yyy: morning
yyy: probably
yyy: see how I immediately stopped getting ready for the office
yyy: thank you, kind person!
xxx: I don't think they'll be doing it all morning. I hope they'll be done by 11
yyy: I'm not taking any chances
yyy: it's an omen
yyy: if you hear construction in the morning, rest assured it's until tomorrow
yyy: and a second one
yyy: if you hear a guy drilling with a rotary hammer not in long bursts but in short sighting shots, know that in life he's very shy and yes, the construction will go on for another week""",
438168: """As a kid I could virtuosically imitate a creaking door in horror movies, purse my lips like a fish and do a bunch of other interesting things like that. Growing up, I suffered from having nowhere to apply such valuable skills: the office wouldn't get it, home even less, on the street they'd just take me to the psych ward.

I went to work at a kindergarten. Happy.""",
438169: """These days lots of places offer to start your own business so you can earn passive income lying on the beach, like Emelya on the stove.
It's kind of like recommending a person tired of workload and stress at work to become a firefighter.
Yes, firefighters have the privilege of sleeping on the job, only they're not exactly paid for that...""",
438170: """comments at dpmmax@lj

ilja_lv: Here in Riga last year we got wild boars. Literally, running around residential areas, digging through trash, strolling downtown.
Then they started catching them and taking them to the forest, counted about 100 animals in total.
Not a single serious incident the whole time.
murashkka: Oh! wow! As far as I know - a sow with piglets is extremely dangerous, can charge at any rustle. And you had it all so peaceful, I'm shocked.
inry_r: Thaaat's the Baaaltics for you..""",
438171: """xxx: When you're sitting around guys born in the 50s who are cheerfully singing student songs to a guitar, and you're sitting there feeling like a dinosaur at 37. An amazing feeling!""",
438172: """They brought a new stove wrapped in bubble wrap. Sitting on the kitchen floor, popping bubbles, the pug came in, looked at me like I'm an idiot, then put his paw on the edge of the plastic, the bubbles went pop, the pug got into it, now we're sitting here popping them together.""",
438173: """Ustas
An international group of scientists conducted an experiment, finding out how many holes can be drilled with a drill in a wooden cube before it falls apart.

Data_Link
the same kind of scientists, only with a concrete cube, live in every apartment block in Russia""",
438174: """Imagine, I asked a group of kids what work dad does around the house, the answers killed me: eating, playing tanks, talking on the phone, swearing, exercising, hugging mom""",
438175: """Geektimes, discussing the "usefulness" of some kids' cartoons.

ххх: You should show them not by usefulness but by variety of material. So the kid learns to separate the useful from the useless on their own.
I watched Animal World all my childhood, and they only eat and screw there — guess my deepest desires?""",
438176: """Complaining about my twins not letting me sleep. I say: Marisha (the girl) first lies there a bit, looks around, then coos, then grunts, and only then starts yelling. And Nazar screams right away, before he's even awake. Lies there all day, and as soon as he wants to eat — he screams.
My sister (with feeling): oh, my Pyotr Ivanovich (husband) is 64, and it's the same picture: lies there all day, and as soon as he wants to eat — he screams""",
438177: """xxx: looking at the downloads folder on an overstuffed drive and afraid to delete anything. in a week I'll need it, even though I haven't needed it for two two years.""",
438178: """ххх: Once I finish my teleportation theory, I'll switch to a Ferrari. And I'll race down empty roads, no traffic jams or traffic cops! And buying gas for pennies.
ууу: And us?
ххх: And you'll teleport like suckers.""",
438179: """I sometimes visit my home village. I've really started liking it here. Especially after living in the city - finished university and got a job. It's such a breath of fresh air here... So I went fishing: outside it's god knows what, under my boots there's slush of mud, snow and water, it's raining. People were heading to the truck that brings groceries. Everyone's gloomy, only I'm walking toward them happy. Everyone asks why I'm so cheerful, and I'm like, just in a good mood. And they've run into me happy more than once.
My friends tell me - the grandmas are now gossiping among themselves: he was never this cheerful before, only since he left for the city did he change. He's probably on drugs!
Facepalm...""",
438180: """They gave my husband a dartboard at work, he brought it home, hung it on the door, the little one died of envy.
Had to urgently buy a kid version with velcro balls.
Hung them on one door under each other.
Now I'm afraid to enter the guys' room: either a dart in the forehead or a ball in the butt...""",
438181: """Discussing ReactOS 0.4 on LOR:

Oxdeadbeef: Can it do x86_64?

Jedi-to-be: It can, but not yet.""",
438182: """About gravitational waves...
- I even know what this device will be called.
- ?
- The Gravitsapa.""",
438183: """Last weekend at one of the well-known home appliance supermarkets. Grabbed what I needed, present the goods at the checkout. The price is EXACTLY one thousand bucks. I hand over a bill, the cashier girl rings up the receipt and puts it on the counter together with a one-ruble coin. "Is that the change?" I ask. The cashier looks at me, at the receipt, and with the word "Habit" takes the coin back.""",
438184: """From a numismatics forum:
gvele: By the way, my wife recently told me that the state really needs money right now and that all of us numismatists will be dekulakized and exiled to Siberia. And she, like a faithful Decembrist wife, will go with me. And Siberia is very cold. And you need to prepare in advance. So come on, buddy, let's go buy me a fur coat!""",
438185: """St.Kitts: there was pasta standing in a closed pot in the kitchen. the cat snuck into the kitchen, climbed onto the stove and started rattling the lid. I go out to shoo the ninja away, as usual he's already dematerialized, but here's the miracle: despite the pasta slightly scattered on the floor, which wasn't there before the cat arrived, the pot was standing on the stove with the lid closed, completely untouched Oo""",
438186: """Eugenich: And back in the early 90s they were stealing a 99... there was a canister of water in the trunk... those beauties apparently filled up from it... and abandoned the car right by the traffic police post... like, lucky .((((""",
438187: """Umin: I'm so broke I had to replace my morning coffee with morning exercise.""",
438188: """Day off, the family is watching "The Martian". Epic moment with the hurricane, when all efforts go to waste, I hear my son next to me starting to sniffle and wipe away tears on the sly:
- What's wrong?
- I feel sorry for the potatoes...""",
438189: """Forum thread about sex:

Xxx: 
My acquaintances split neatly into two opposite camps on the topic of open relationships...

Yyy:
"My acquaintances undressed"
Not a fan of pure psychoanalysis, but has Freud by any chance confused you?""",
438190: """1.01.2016, an ad appeared:
TTT: Hi everyone. Giving away a Christmas tree, 1.6 meters of your choice, didn't need it. Drove out past Vyborg, chopped it officially. Picked the thickest and straightest one on the clearing.
ууу: still relevant XD
ххх: You should've offered it on May 1st, you'd have no shortage of clients.
QQQ: How's that? Did they cancel New Year's for you?
RRR: I'll take it, in exchange for a box of last year's snow. Pickup only.""",
438191: """xxx: a friend of mine defended her costume design degree to opera music from The Fifth Element""",
438192: """M: - Eh, Little one, you don't know how to get treated(( Like a child honestly, "it buuurns" she says
W: - No, it really hurts a lot. Feels like it's burning the mucous membrane in my mouth.. even my tongue. Aloe is okay. I'll try that one.
M: - That's no fun way to get treated. I'd douse it with fir oil and rinse with alcohol. It's called "Give the illness no chance!"
W: - I remember how you dripped that fir oil of yours into your nose for a runny nose. And how it got into your middle ear, you went deaf for a month.
M: - That's nothing. My dad burned off his warts with a cigarette butt. Got rid of them completely. And he "removed" his army tattoos with quicklime - now that's our kind of man! And I'm generally afraid to tell mom about the fir oil. She'll immediately try it as an enema)))
W: - Yeah, your family is dangerous when it comes to treatment!
M: - We're hardcore - either you recover, or you're free!!!""",
438193: """xxx: Strictly speaking, the article discusses surrogate keys. And, ideally, using them as primary keys is bad, because it violates data semantics. And of course it's ignored everywhere

yyy: On the forum a user named Vasya Pupkin sent another user named Vasya Pupkin a message "Hi, namesake", and managed to press the "Send" button 2 times in a row within one second. What semantically significant properties of the "User" and "Message" entities would you choose as their natural keys?""",
438194: """from a discussion of the MFC

- what certificates does the governor need to bring with him?

- No-no, he has to find that out himself! That's the quest hook, the preliminary stage! He has to talk on the phone with a lovely woman who'll list a few documents for him, and then, at the window, it'll turn out a whole suitcase of papers was needed, and "they couldn't have not told you!" Don't deprive a person of pleasure. :D""",
438195: """Ххх: "... And if you unintentionally slept with a man or cattle..."
Ххх: I dunno how you can accidentally sleep with cattle!
Ууу: Well, if you slept with him and he turned out to be not only a man but a complete beast...)""",
438196: """Ann:
The most fun activity is building a website drunk after the corporate party. Databases seem so funny!""",
438197: """News: "A group of 20 fake psychics was detained in Moscow on suspicion of fraud".
What, are there any other kind of psychics?""",
438198: """xxx: looking for recipes on one site
xxx: site map:
xxx: INTERESTING ARTICLES/ SLOW COOKER DISHES/ OVEN DISHES/ MEAT DISHES/ POULTRY DISHES /FIRST COURSES/ SWEET BAKING /WANT TO LOSE WEIGHT
xxx: truly the cycle of a woman's life :DD""",
438199: """Avis Hermetis: By the way, I never knew how to ice skate
Avis Hermetis: but a couple days ago I tried
Avis Hermetis: long story short, they took 100 rubles from me, my shoes, and made me suffer for a whole hour
Avis Hermetis: fell about 40 times, then lost count""",
438200: """Every day I go to the dacha to feed the kitties abandoned by a despicable ex, and in this connection I'm really fussing over the menu. Now the poor orphans Pesya (5 kilos of ginger fluff and meat) and little Musya, instead of cheap kibble, eat turkey, soups, chicken giblets, necks, hearts, liver, meat trimmings and porridge in broth at my place.
But today the chef, so to speak, outdid herself: I cooked them soup from red fish trimmings with potatoes and carrots plus 4-grain porridge. Pesya the bastard showed up already full, snacks somewhere else, Musya spoiled her appetite with chicken and started turning her nose up at the grub too.
Well I'm hungry, frozen, tired... Couldn't resist, in short. And what, it's tasty, fatty, filling.
Forgive me, guys, tomorrow I'll cook you a new soup!""",
438201: """ххх: How long can you have sex?
ууу: Until the internet runs out""",
438202: """forum. Photo of a partially blacked-out large-family certificate, where 4 out of 5 kids were born in December. Author's comment: "That's how you congratulate your wife on March 8th!\"""",
438203: """The blueprints they sent late Sunday night have no doors. Apparently every room is meant to be entered by apparating.
I wouldn't care, but I need the door sizes for evacuation calculations. I write the architect an email: 2 AM. At 3 AM I get a reply: no blueprint, no doors, with the phrase "how you've all pissed me off". Took the words right out of my mouth.""",
438204: """frost16:
Sliced a lemon with a Faberge table knife. Nothing else was at hand. Fun working in an antique shop :)""",
438205: """lj irkuem:

About "Deadpool"
Decided to go in the morning. At the cinema we ran into a line of about a hundred and fifty people. Turned around and went home. Bad, worthless movie.""",
438206: """One neighbor on my floor sleeps during the day, and at night from 1 AM he constantly blasts music with bass, I go over to "talk". He turns it down, and the next night it's the same again... So I come over one night - another neighbor says he just moved in, no music... Strange. I go back to my place... Silence! I lie down... And hear muffled bass. I get up... Silence... I lie down - bass... The pillow pressed tight against my ear was "bassing" me my own heartbeat. And the new neighbor's quiet)""",
438207: """ххх: ... but then they said the server's IP address ends in ".376" and I realized we'd be here a while.""",
438208: """xxx: did your battalion ever tell a general to go fuck himself?)
a general once came to our brigade, showed up at formation, "hello, comrade soldiers", all by the book, each of us thought to himself that if he says "go fuck yourself", against the "we wish you health, comrade general" of everyone else it won't be heard. Yeah right, 50-70 people in a single burst without conspiring tell the general off, he takes about 15 seconds to recover, says "hmm, let's try again".. the second time it came out as required)""",
438209: """xxx: Fresh-born joke - how many IT guys does it take to move a server to another subnet?
xxx: Answer: five. Two testers, two engineers and an admin.
xxx: The testers need to but they don't know how, the engineers know how but they're not allowed. And the admin let everyone into the server room.""",
438210: """I love chatting with the Chinese on AliExpress.
Bought my little brother a solar-powered robot constructor kit. Writing the seller in the order comments, asking if he has another battery, since my brother lost it. Reply: "Take a photo of the part you need, we'll see if we have any." Take a photo, bro, of the lost part!""",
438211: """I work from eight in the morning. I leave home at seven-oh-something accordingly. Morning routine is timed to the second and everything gets done without thinking...

I go outside and realize: I overslept. I was supposed to leave at the crack of dawn, but it's LIGHT out. I wonder for a second how that could be, I got up by the alarm...

In short, that's how I realized spring had come and the days got longer.""",
438212: """An old acquaintance called once and said - a year ago you built me a computer, could you swap the CD drive for a more convenient one?
- doesn't read CDs?
- it reads.
- doesn't read DVDs?
- it reads.
- doesn't burn?
- burns everything.
- hmm, so what's wrong?
- it's inconvenient.

He brought the computer over, turned out - the CD drive was installed upside down. (I was building it with the case on its side, on the desk)
Flipped it - and the man is happy again - now you don't have to hold the disc when inserting it. And it doesn't fall out when it ejects.""",
438213: """We partied well once. Dragged one-completely-out-of-it guy to his place. His mom opened the door, looked at him, sighed calmly and doomed, calmly said:
- Well drag him to the room, what else can you do.
We dragged him in. Laid him on the bed. Helped take off his jacket. Then the body wakes up, looks at his mom and with great difficulty pronounces:
- sorry, I kinda overdrank.. Can I spend the night at your place?
WHAM!!! Mom sends the body into a knockout with a mighty slap and just as calmly: guys, thanks for the help.
The next day Andryukha asks us:
- Guys! Why does my face hurt? Why's my cheek red? Who did I fight yesterday?
- Well, with one woman. By the way, she knocked you out!))
- What?(( What woman? Which one? Which one? He kept pestering us after that)""",
438214: """My wife has been retraining our kid to a new daily routine for days now. Sobbing, screaming, throwing bowls of porridge and demonstrative hunger strikes - equally on both sides. I want to hang a sign on the apartment door: Concentration camp "Sunshine". And not go in...""",
438215: """Discussing the question
xxx: And even if we're like the swan, the crayfish and the pike for now, we have a goal, and it's like a carrot, and
the carrot turns the swan, the crayfish and the pike into a single mechanism moving in one direction.
yyy: O.. and what the hua-hua do the swan, the crayfish and the pike need that carrot for? Am I missing something about them?
zzz: Actually it's donkeys who walk after a carrot tied in front of their nose..""",
438216: """headline "Toshiba, VAIO and Fujitsu to merge computer divisions"
and VAIO will get the biggest share in the joint business

Interesting, since VAIO is the boss, the abbreviation will be - products of VTF?""",
438217: """I work at a pet store. New goods arrived, including a cat house. The director calls and says: "Barely found the price of joy". (that's what the house is called, actually)...

And sometimes it happens that loyal friends run out (it's a pet food brand)...
That's how we live...""",
438218: """In the trolleybus an elderly woman was telling her friend:
- At the clinic I was redoing my medical card, they wrote the diagnosis: pensioner.""",
438219: """My husband got sick, temperature just over 38, lying on the couch, moaning, demanding a cold compress on his whole body... The cat is on its second day recovering from neutering, lying on the other couch, moaning, demonstrating a serious case of the shivers as post-anesthesia comedown, literally freezing all over, wrapping itself in little blankets and demanding additional warming methods... I spent half the day running from one sufferer to the other, hauling cold compresses to one and hot-water bottles wrapped in towels to the other... Until it dawned on me. Husband and cat are lying hugging on one couch, sleeping a happy carefree sleep. The cat warms itself on the feverish owner, the husband cools himself on the freezing cat.. On the freed-up couch sits yours truly, pleased with herself, embroidering =)""",
438220: """<Sotona> god bless blind people
<Sotona> it moved the shortcuts around and now can't open "my computer" because "waaawaa nothing works"
<Sotona> for this I eat bread with caviar every day
<Sotona> but on the other hand, the calls at all hours of the day and night are driving me nuts""",
438221: """My wife is doing a deep clean of the house, I'm battling the vacuum cleaner. She found an old plush mouse, comes up to me showing the mouse:
- Look, this mouse is 20 years old. It's older than most of your opponents in online games. And smarter...

awakeman""",
438222: """xxx: Damn. The art of crimping twisted pair with a screwdriver is lost... A competent specialist, in complete darkness in an awkward spot, crimped with a screwdriver almost faster than a specialist with crimpers.
yyy: screw the screwdriver! there was a specialist whose "gold" filling was so sharp it fit right into the slot. bit down and no need to check! now the specialist is still around, but the crown is gone... the art is lost.
zzz: Damn, that's an idea... Order myself a tooth crown with a crimper comb.""",
438223: """xxx:
Here come the kids with the husband, trying to congratulate our feisty granny. No such luck! "The bouquets are way too big!.... And besides, your flowers took up the vases, and the cat likes to drink from them..", had to explain that he already has watering cans and 2 "his" vases in use, everything else is off limits, and anyway he devours roses. Shamelessly :P
Then the table got mildly criticized, with lamentations "there's kind of little.... don't you think?!" (although honestly the table was groaning under pies and pastries, all kinds of pickles, salads and even (horror!) sliced deli meats, which are banned at our place)
In short, until 3 PM granny methodically and systematically blew everyone's brains out, including the chinchilla, which had the nerve to "sleep in an unnatural pose, and during the day at that!\"""",
438224: """zem: they should've made August 32nd instead of February 29th !!!""",
438225: """xxx: Three pairs of scissors, six nail files and an axe in the house - and you bite your nails!""",
438226: """Sad first workday after the holiday weekend. Everyone in their own corner in dead silence doing work.
Completely exhausted, I exhale without thinking "sup, dvach!.." And from every corner, in a male bass, an equally cheerful "mrmrmrmrmr".
And again a minute of dead silence, and then the verdict: "no one must ever know about this".""",
438227: """From opennet:
These pathetic bicycle lovers have simply never tried riding through the morning park in a jeep!""",
438228: """Connecting remotely to the warehouse lady.
- Lena, do you see the "Your ID" field?
- No! It's writing me something like "proxy server"! Oh! I see your id! Write it down, in capital, big letters
"E"
"R"
"R"
"O"
"R\"""",
438229: """xxx: As a kid there was "Kolokolchik" lemonade, in adult life the same "Kolokolchik" is Bell's whiskey...""",
438230: """Xxx: well, now strictly till 5. freebies cancelled :)
Xxx: 8 to 5
Yyy: did they introduce passes?
Xxx: no, they're watching like this for now :)
Yyy: on cameras?
Xxx: why? the HR engineer sits by the entrance morning and evening and writes it down :) nanotechnology, you know :)
Xxx: we haven't grown up to cameras yet :)""",
438231: """(Before wiping Windows on the comp)
Alexander
was thinking of filtering your files somehow, what to copy and what not

three minutes later I was floored by your new folders and decided to just copy the whole profile - you dig through it yourself later, where you have boob pics and where you have kitty pics

Lelya
That stuff definitely isn't there)))

Alexander
everyone has boob pics and kitty pics on their comp

Lelya
Everyone except me)

Alexander
fine, I'll put some on for you, it's not right to be without boobs and kitties""",
438232: """Another perky boy called the home phone. Started brazenly pushing an urgent water meter replacement, which of course is actually fine at our place.
I was in a playful mood, decided to break the guy's script a little.

- Good afternoon. You urgently need to replace your cold water meter. When is it convenient for you to receive the technician - today or tomorrow?
- Hello. Neither today nor tomorrow is convenient.
- But you absolutely must change the meter!
- Why?
- Otherwise you'll be charged for water by the building-wide meter, and that's many times more expensive.
- Fine.
- Uhhh... Goodbye.""",
438233: """Decided to change my front door. In the store, two salesgirls immediately latched onto me and started praising the doors, with particular fanaticism pushing that as a gift I'd get a totally amazing bulletproof peephole. Then the dialogue:
me: And is the door leaf bulletproof too?
they (o_O!): No, why would you need that?
me: So in case of an armed attack, the only thing guaranteed to stay intact is my right eye?
Mumbling something like: "need to ask the senior" they fled into the bright distance, leaving me alone with my thoughts about the eye.""",
438234: """xxx: got screwdrivers with you?
yyy: Phillips 2 big, 1 thin. Flathead 1 big 1 thin
yyy: Well I always carry half a set in my backpack)
yyy: Plus a tape measure and a knife
xxx: what's the tape measure for? %)
yyy: What if there's drinking and an argument about who's longer?""",
438235: """B1ack: Deadpool was quite original relative to all the rest of similar crap.

Octavius: Yakubovich, with a cigar in his teeth grinding crowds of zombified people with a combine harvester and yelling "SPIN THE WHEEL!!" - THAT's original. Deadpool just has good humor and action

AlanWake: I played a mod like that for Skyrim too...""",
438236: """Mother of many:
xxx: My kid, doing homework about the family, in the "hobby" field wrote "sleeping" next to my name. And I'm not sure they're wrong.)""",
}

if __name__ == "__main__":
    print(json.dumps(EN, ensure_ascii=False))
