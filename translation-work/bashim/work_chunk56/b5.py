#!/usr/bin/env python3
"""Translate bashim chunk_56 batch 5 (input lines 401-500, IDs 438537-438636)."""
import json

EN = {
438537: """xxx:
A story from my not-so-distant youth. Came to pick up a friend for a walk, standing at the door, Ksyukha runs around getting ready. Her mom walks around grumbling - you go to those discos of yours - you drink there, and smoke, and swear!!!
Ksyukha from the other room indignantly - I DON'T SWEAR!!!""",
438538: """A colleague walks into the office and passes on a message:
- The director is coming here, he'll sort out why the problem still isn't solved.
Everyone tensed up somehow, and another employee suddenly starts singing:
- And the boss is coming, the boss is coming. And everything around is waiting for soooomething...
And everyone knew what :(""",
438539: """Came up with a new character!
By analogy with the gorgon jellyfish, the jargon jellyfish.
When you look at her, she speaks thieves' slang and swears wildly.""",
438540: """Just had a meeting about the upcoming release.. Sitting laughing with my wife.. .
"In connection with the postponement of the publication of the release containing critical fixes to March 24, the working group meeting of March 3, 2016 resolved: the wife of employee Ivanov I.I., responsible for the distribution, shall NOT GIVE BIRTH before Thursday.\"""",
438541: """xxx: 2045 is, roughly speaking, 30 years away. 30 years ago they promised us flying cars, airboards and Skype without lag, so where is all that? :)
yyy: well, Skype without lag — that's a stretch.""",
438542: """x: Sitting reading a book about raising kids, the author is our Russian - a modern psychologist, so there are good thoughts. Need to find some more literature about kids.
x: Ok, going to sleep, the little one will wake me in the morning
y: G'night!
x: Spock I've already read! Total crap!""",
438543: """aaa: The fact that no one has yet made a series about economists is deeply offensive. Every channel has series about police, crime, medical workers, and finally lawyers. And judging by conversations with actual police, lawyers and doctors, the series are about as similar to real life as I am to a ballerina. They even made a movie with Clooney about an HR guy, so what makes economists worse, I ask? Our lives have drive and adventure too. I, for example, spent a whole weekend calculating external debt as a percentage of GDP starting from the god-forgotten 1995 with one hand, and moving and packing with the other - solid emotions and heated passions, I tell you!""",
438544: """xxx:
Well actually, that's the only downside of modern tech
It becomes obsolete too fast
It gets fun when updates stop coming out for the iPhone you're still paying off a loan for.""",
438545: """News:
EXTENSIONS APPEAR IN MICROSOFT EDGE BROWSER
"...In the near future the list will expand noticeably: it's known that Microsoft is cooperating with the developers of AdBlock, Adblock Plus, Amazon, LastPass and Evernote."

Comments:
xxx: good braz will install adblock first thing
yyy: Install chrome first thing""",
438546: """Discussing an accident on ru-chp:
- the number of Toyotas per sq. meter in Chelyaba is off the charts) are they stamping them out at the factory there?)
- Didn't you know? The famous ChTZ - Chelyabinsk Toyota Factory.""",
438547: """xxx: Night shifts are already sitting in my liver... Writing my resignation in the morning. To hell with them.
yyy: I worked 8 years as a press operator (count 2.5 years in the night shift). When I became shop foreman, I canceled night shifts. The workers hung my portrait in the shop :D""",
438548: """Zoya: Imagine, got out of the shower, and Beardy suddenly started rushing around the apartment. Peeked into his secret closet, rummaged there, thought, came up warily, pecked my cheek, genuinely sniffed and said tenderly:
– Oh! So it's my beloved wife washed her white breasts and smeared them with fragrant sea-buckthorn cream. And I thought – the mash leaked...""",
438549: """So, say, we have a cat and a hamster. The cat is a red lazy carcass, all day playing the role of an extra couch cushion. Plays well, with soul. The hamster is a bit off, he got a skull injury and went mad. At night he escapes the cage and scares my wife, pretending to be a cockroach. We had a fear (and someone - a hope) that one day the cat would catch and eat him. Yeah right!
Catch - he did catch, why not...
An hour after the escape the kids found and took away from the cat a confused and humiliated hamster. Licked all over.""",
438550: """xxx: Cinderella and Sleeping Beauty have been rebooted so many times that now we should expect something like "Cinderella vs Sleeping Beauty: Civil Prince" or "At the Dawn of Marriage\"""",
438551: """I walk into the room. My daughter is sitting on the floor, wrapping herself in a small baby blanket. I say - what are you doing? "I want to be a sausage". And at her age I wanted to be a ballerina...""",
438552: """stradetch: Thanks for the game
CrocodileAnus: my respect
stradetch: That awkward moment
stradetch: when you're respected
stradetch: by a crocodile's anus""",
438553: """Project development stages:
Denial - the team refuses to accept the customer's wishes
Anger - aggression toward everything around manifests
Bargaining - thoughts appear about negotiating an alternative implementation with the customer.
Depression - the team is in development around the clock
Casting""",
438554: """ctokep: Thanks to our local electrician, our whole district took part in the "Earth Hour" campaign four times in one day""",
438555: """About kids' love for animals you say? At my place when any kids enter the house, chaos starts, the cats hide so you can't find them, the dogs immediately to the yard - can't catch them there. You're laughing, but once my Musya dawdled half-asleep, got caught by two boys, my wife's friend's kids, when they left she could barely stand because she was their driver (of a plastic basin and then a bucket, apparently different models) and pilot (drove a three-liter jar, barely took it away), cosmonaut (underwent pre-flight training in an empty aquarium) and then "treated" after falling from the plane in a hospital swaddled in a towel (mom is a medic), so kids' voices in the house don't please my animals...)))""",
438556: """xxx: once in Tomsk there stood a monument to the wooden ruble
yyy: and then it fell?""",
438557: """xxx: How's it? How's work?
yyy: they put a couch next to the toilet and called it zoning""",
438558: """<Angelofnet> In cinemas across the country watch the film "Batman vs Superman: two morons are a force"!""",
438559: """My cousin brother (13) was visiting and stumbled upon all kinds of computer junk in my closet. Learned a lot of new things: about 8-gig hard drives, that there used to be small-volume flash drives, i.e. floppies (he didn't know about them, I'm in shock). You should've seen his eyes. But in the end I finished him off:
- and here's an even older paper flash drive.
I hand him a punched card. He looked at it, at me, then 3 more times like that. The whole evening he sat thoughtful and didn't ask more questions. Either he didn't believe me, or I just broke him))
Later his mom calls:
- why are you messing with my son's head? He came with some cardboard, says it's a flash drive.
- well, it is! A flash drive from the 19th century!)
- okay, don't mess with my head and stop mocking your brother! Bye!
Hangs up.""",
438560: """8:05 AM, developers' room at a large media holding. Phone call: "Hello, this is the prosecutor's office bothering you."

In general, the developer didn't exactly go gray, but his eye twitches a little. And the prosecutor's press office, I hope, will start introducing itself properly and writing down the editorial office's phone number without errors.""",
438561: """xxx: If humans evolved from monkeys, why do these monkeys still exist?
yyy: If Americans descended from the English, why do the English still exist?""",
438562: """<Манул Рамзайка> A girl cried into her pillow for 5 months because of me, and I didn't even know
<Манул Рамзайка> life, why don't you send push notifications for events I'm subscribed to.""",
438563: """lex: Dude, this business idea is as win-win as selling sunglasses in St. Petersburg 8)""",
438564: """Due to the fact that our electric hotplate has lately been working terribly, our menu, besides spaghetti "al dente", now has vareniki "al dente", rice "al dente" and even, sometimes, instant noodles "al dente".""",
438565: """ххх: My tongue is boiling)
ууу: torturing it to make it talk?
ххх: Something like that)
ххх: If it won't talk - I'll eat it)""",
438566: """— Ha! At a village restaurant in Tuscany we booked a table for the evening - for tonight. When we came, we couldn't find ourselves in the list for a long time. Turned out that we - four foreheads - are the Fortunato family.
— I already told once that at Starbucks I call myself Gosha, because "Georgy" gets written on cups very illegibly and they call anyone but Georgy. Once I took a cappuccino cup from the counter and it said "Horse" on it. So Fortunato is a good surname, take it.""",
438567: """Artur: Nice autocorrect
Artur: *shitty""",
438568: """Comments in "Typical sysadmin"
xxx: Here we go... Why do you think anykeys are always so ham-handed? On the contrary, they're more curious and hardworking, unlike admins.
yyy: > more curious and hardworking
That's the whole trouble and horror.""",
438569: """xxx: my sister and her husband adopted a dog from the shelter - an adult mongrel already, knows its name. The funny thing is the dog is called Fedya - and, oh coincidence! - my sister's two-year-old son is also called Fedya! well they laughed, decided to henceforth officially call the son Fyodor
ууу: and everyone's happy))))
ххх: not everyone. when I came to my nephew's birthday and handed my sister a little bag with the words "bones for Fedka", the part of the family not yet acquainted with the dog looked at me like I was a freak""",
438570: """xxx: Owls can turn their heads 270 degrees... at the lark that decided to make noise in the morning.""",
438571: """Long ago, even before the rabbits, we bought the kids two Syrian hamsters.
My mom asked what we'd do if one of the hamsters died.
The kids were little then, so I said: "I'll hide the corpse, quickly run to the pet store and buy exactly the same hamster."
"Good idea, - said mom, -we always did the same to you!"
ALWAYS! More than once!
My worldview fell into sediment...

LJ marinalevy""",
438572: """< pLuto> the Touareg and Cayenne are being recalled due to pedal problems. apparently it's hard to pedal :)""",
438573: """xxx: Waiting for the article "Stop abusing classes!"
yyy: "How to stop programming and start living", might as well""",
438574: """A crowd stands. 80% of people in the crowd wear jeans. Half of them have iPhones. About 80% of these people go to work in the morning and work for the man all day till evening. More than half of them hate Mondays, barely wait for the end of the work week and blow off steam, each in their own way, on Fridays. Half of them live on credit. Three quarters of the crowd are married, or aim for marriage. For more than half of them the synonym of success is having a multi-room apartment, a cool car and the opportunity to travel abroad every year. 95% of people in this crowd say the phrase: "I'm not like everyone else".""",
438575: """xxx: You haven't yet come across the Hormone jellyfish. When the imbalance hits its head — just cover your ears.""",
438576: """xxx: typical St. Petersburg...
xxx: one drunk tells another: "you're a keyboardist, and I'm a string player, that's why you don't understand me!\"""",
438577: """xхx: During the management's discussion of layoff candidates the phrase was uttered: a good businessman won't slaughter the rooster with golden eggs.""",
438578: """xxx: Haha. Here in the metro a couple is playing tic-tac-toe on a phone display!!! Like, traces stay when you run your fingers over it)) original
yyy: That's the coolest phone app I've heard of :)
xxx: Well with the same success you can play on a monitor ))) with your hands)))
yyy: And that's called cross-platform :)""",
438579: """How to ruin the morning for parents and the kindergarten teacher? Simply bring the kid a suit! All parents immediately panic: "What, we were supposed to come looking nice? What, do they have an event today? Why didn't anyone say? Why didn't the teacher warn us?", the men urgently start calling their wives with similar questions, and then all together terrorize the teacher, who has no idea why this particular kid got a suit today. And it's actually simple - after kindergarten we need to go to granny's anniversary, and there won't be time to go home and change... =)""",
438580: """xxx: Today I'm on throat sucking candies
УУУ: Sucking candies it is
ххх: Yeah, I want to suck for my health!""",
438581: """I'm the head of the technical control department, conducting a briefing.
One of the inspectors:
- Some fly bit the welders in the shop today and they started welding according to the drawings!!!
I carefully ask:
- And what did they weld by before that?
- And before that they welded as needed!
Got thoughtful. Need to go to the designers, find out the difference.""",
438582: """xxx: Friends, be happy for us. We finally hired a really cool and strong PR guy!
yyy: well, thank god
zzz: congrats, good luck with him )
xxx: He just smashed the sink in the toilet to smithereens...""",
438583: """ML: A guy half sticking out of a stroller, leaning toward the child, quietly broadcasts from there: "You see, modern science still doesn't have confirmed data on the existence of extraterrestrial life, so I can't tell you exactly what color aliens are\"""",
438584: """Discussing journalistic idiocy in a report about the launch of a ship to Mars

dbanet: Didn't get it. Please clarify, is it true: the fact of the upper stage engine operating in four non-overlapping time intervals was interpreted as "launch on the fourth attempt"?

lozga: Exactly that.

Halt: Be thankful they didn't write "the engine stalled 4 times".

Oberon812: And at the end the booster block fell off entirely!

Halt: Then like this: "During the launch the engine stalled 4 times, and then the tank fell off entirely. The rocket itself literally fell apart in flight!\"""",
438585: """mishoot 19.03.2016 at 16:14
And here's a story from the other day told by the respected IKOM.
» A group of comrades from Nalchik, who came for a two-day rest to the Azau glade, closer to midnight, climbed from the glade to the track turn (above the top of the Azau drag lift), untied the mat that covered the snow cannon from careless falls of riders, settled on it, pushed off and rushed down the flat-as-a-billiard-table groomed track, which by night the frost had also gripped. Breaking through the finish fence netting, the company together with the mat (meaning the protective mat untied from the cannon) knock out the rental shop's glass unit and fly, like in American thrillers, into the rental premises. Alas, one person in intensive care, the rest got shrapnel wounds and compression injuries of various severity. I won't comment.»""",
438586: """ДДД: A spiritist session? Tempting! Prepare the props, don't break the glAsses, I'm coming.
ДДД: And foOd. I'll be your priestess.
МММ: A priestess of love?
ДДД: A priestess of food!""",
438587: """Remember the joke about the customer in a computer game store:
- are you choosing which game to buy?
- no, I'm choosing what to download from torrents.
I remembered this joke when I was picking a backpack in a store, and then ordered the model I liked from my phone online, since it's cheaper there""",
438588: """When once again the internet speed dropped to turtle-like, I called tech support and with surprise heard that everything is fine on their end, it's probably my internet cable that's damaged or pinched somewhere. (Aha! apparently the cable cross-section shrank and bytes started getting stuck). But I changed providers not for that, but because this company demanded payment for every device connected to the router.""",
438589: """Koravek: ...need to teach kids how to protect themselves from one-time maniacs.
my sister in childhood had the opinion that if a person smiles - they're good. so the parents at a fairly tender age showed her "The Silence of the Lambs" to reset that setting.""",
438590: """Al Kopernik: Let's mentally return to the joy called Skype. Astonishing, but even in your thoughts you'll have to wait a couple minutes for it to load...""",
438591: """Vadim: Do you have a 1C admin? Who maintains it.
Director: )))))
Director: some bastard
Vadim: Give me her number.""",
438592: """Bought sour cream for blini, mother-in-law gasps that I took too fatty - village-style, 30%.
Justifying myself:
- Well it's tasty! I always choose fattier for myself.
Wife, proudly pushing out her size-4 chest:
- For example, me!""",
438593: """Saw a beautiful thing:
Walking down the street. Leaning against a wall and putting a cardboard under his butt, sits a vagrant of a totally typical look - an ushanka hat with sticking-out ears and hanging ties, a gray tousled beard, shag under the hat. Clothes heavily worn but neat. And above him on the wall a spray-painted inscription: "The bottom is much closer than Gorky thought".""",
438594: """x: Honey, I'll be late today, our balance doesn't converge
y: By much?
x: By twelve kopecks
y: And you'll again sit three hours at work because of twelve kopecks?!! Add your own and come home already
x: How do I explain it to you? Remember, last week you told me about the checksum? Well look at these kopecks as a checksum. If an archive's checksum doesn't match, you re-download the archive, and if the balance's doesn't - you recount. And it'd be better if it didn't match by 12 thousand, then the error is most likely one. But twelve kopecks - that's at least two errors, need to recheck everything""",
438595: """xxx: So! Who told the grannies that you can use Yandex to see where the bus is? Now they demand at the stops that young people "check the app".""",
438596: """Dad (almost 60) is a typical Soviet engineer. Of those who love fixing everything that broke with a soldering iron, a roll of blue electrical tape and such-and-such mother.

In short, at my parents' home the radio receiver (the wired broadcast one) broke, and mom is bored puttering in the kitchen without it. Dad started removing it to fix it, and accidentally tore out the socket the receiver was plugged into - it's flimsy. Returning the socket to its place, he accidentally slipped on the stool, to not fall grabbed the cupboard door. Didn't fall, but the door had to be fixed. In the process of fixing the door he broke a screwdriver (now that's a real mystery). In the receiver, as it turned out, a wire had come unsoldered; soldering it back, dad dripped solder on the countertop (laying newspaper - that's for suckers)...

"In short, - mom sighed heavily telling this story, - saving 300 rubles still costs very dear!\".""",
438597: """Lying with my kid in the infectious ward. My husband can't cook and brings me ready food from the supermarket.
As it turned out - eating salads from the market right in the infectious ward is ooovery thoughtful!""",
438598: """xxx: just getting into the code
yyy: get deeper into it
xxx: no one's complained yet about how deeply I get into the code
yyy: yeah no one just noticed you getting into it""",
438599: """It turned out very interesting with the employment center psychologist. She started asking standard questions, like how the search is going, what are your strengths/weaknesses, what you want in general, etc. Word by word, talked for probably half an hour. At the end of the conversation she goes: find a job, keep me in mind too.""",
438600: """My friend quit smoking and stopped drinking alcohol. A cry of the soul on Facebook:

#lifeIsARasp
Gentlemen, I've hit rock bottom. Never has a hashtag so strongly matched the mood.
I smoke a nicotine-free e-cigarette and drink non-alcoholic beer. What's next?! (heartbroken) A rubber cat?! (sobbing)""",
438601: """xxx: Still going to the store on sabbath?
yyy: You witch of mine. Yes, on sabbath we're going to the store
yyy: And we need to disable autocorrect on your keyboard""",
438602: """хxх: catching a virus isn't as scary as catching a Chinese antivirus.""",
438603: """xxx: Took the middle one to a psychologist about stuttering. The psychologist asks him who's in charge in the family. The kid quickly answers: "Dad." Next question: "Why did you decide that?". "Mom said" - answered the son. The psychologist kept stealthily trying to pry out of me who really is in charge, and dad laughed for a long time.""",
438604: """She: How can I root for one of these teams, if to me they're just "the blue ones" and "the green ones"... And those aren't even my favorite colors""",
438605: """from the office complaint book:
While waiting in line for service, we read your complaint book. Laughed a lot. Thank you. Could you put the previous parts on display? The service is very slow, we didn't know what else to do.""",
438606: """LJ melicenta77

Mandatory medical checkup at the institute: one fine day the whole course was ordered to come to the polyclinic we were attached to (at the opposite end of Moscow from our faculty, and I lived in the suburbs altogether, so getting there was especially fun). Everyone was given medical cards and a huge list of doctors to necessarily visit was posted. And at each office there was already a queue of 5–10 people. The examination, of course, was a pure formality – the dentist, for example, just randomly drew on the jaw diagram a generous scattering of fillings and a missing tooth, though I had no fillings at all back then. And the other doctors in the same spirit. But still, because of the queues the process stretched endlessly. And you must visit them all no matter what, otherwise they won't credit you for physical education and won't admit you to exams.
After a few hours I go in to about the eighth doctor. He turned out to be a psychotherapist. And then he asks the sacramental question: "Tell me, have you never wanted to kill someone?\"""",
438607: """xxx: Just rewatched all seasons of "The X-Files", now I'm afraid of not just everything, now I'm even afraid to exist.""",
438608: """Phone call

Solid voice: Hello!
Childish voice: Hello.
Solid voice: I'd like to hear ***ya ***cha, the director of "####" company. Can you call him to the phone?
Childish voice: He's pooping.
Solid voice: ... Alright, I'll call later. (dial tone)""",
438609: """xxx:
Returning the laptop from cleaning.
Apparently there was a ton of tobacco ash inside.
The guy working there reported: "Smoking harms your laptop\"""",
438610: """xxx: Well if he starts insulting us, then his ban will get closer.
yyy: Never gonna happen! I'm a cultured moron.""",
438611: """- We had a huge dude at work with the nickname "Smiter".
- Was he dangerous?
- Nah. He just constantly reeked of hangover.""",
438612: """xxx: Just literally realized) When on the road you come across a "boy racer ride", from which "hardcore" "blasts sick music", it's not just for nothing, it's a concert-car.""",
438613: """xxx: My lead comes up to me
xxx: and says "how are things with the release?"
xxx: and I answer "weell all ok, all standard"
xxx: and then it starts
yyy: what starts?
yyy: life is pain, half a kilo of introduced regressions and a Java senior's hysterics?
xxx: all Java seniors are kept from us behind a wall
yyy: Bgg.)) And if you behave badly, do they raise the wall?
xxx: they reinforce it from the other side
xxx: and then you can hear someone praying in a whisper""",
438614: """ххх: And anyway, who can be cooler than a level-90 shaman?
ууу: A level-90 shaman's mom.""",
438615: """Wife is reading Ancient World history.

w: I wonder what the Scythians really meant, sending Darius a mouse, a frog, a bird and five arrows? Maybe it wasn't a threat? maybe they just sent him their delicacies?
m: what do you mean delicacies?
w: well the little animals could be put on arrows, fried and eaten.
m: and the other 2 arrows?
w: and on these arrows Darius could put eggs, fry and... *falls silent* Yeah, probably it was a threat after all.""",
438616: """Greedangel: Sasha, don't remember what our fax is?
Dragon: well nobody knows it, and Dasha says we don't have a fax
Greedangel: but Anna sent a fax right in front of you
Dragon: I myself just asked, including Dasha
Dragon: we don't have a fax number
Greedangel: but Anya did send a fax
Dragon: I'm not arguing""",
438617: """Lesha: And to hell with these conobuses, can be made much simpler. Between stops build such huge passageways-galleries of plywood with bus-shaped windows. Like at a stop you enter the rear of such a bus and then walk through the cabin to another stop, paying the conductor on the way. There you exit the front door and transfer to the rear door of the next one.""",
438618: """xxx: The cat is still sulking, at me now, question how to make up with a cat?
yyy: Oh come on, yours is definitely bigger anyway, spare the cat's self-esteem )))""",
438619: """windows update burns: offers me to upgrade to 10 with the wording: "Improved security from Windows 10: No one will know what you did last summer"
So tempted to add "...except us\"""",
438620: """Answer to a comment on bypassing captcha:

xxx: create a canvas, place an image on it. go over it and for each pixel calculate brightness brightness=Math.round(0.3 red + 0.59 green + 0.11 * blue); then use Otsu's method to calculate the threshold and binarize the image, then need to rotate letters, train neural networks etc.... just forget it in short.""",
438621: """Most of all in the sanity test I was infuriated by the task where I had to find common features of a rooster and a glass: you can find an infinite number of common and different details for any two or more objects.
I said that a rooster and a glass are homeomorphic, even tried to give a popular explanation of homeomorphism on fingers - figuratively, of course.
I was told my answer is wrong and inadequate, and that actually a glass and a rooster have nothing in common.""",
438622: """PolyMorph: Power overwhelms me! And potatoes with chicken.""",
438623: """The Romanian national team players will come out for the friendly match against Spain in jerseys with equations instead of numbers. This was reported on March 27 by the Romanian Football Federation website.

xxx: While the opponent remembers school, we'll score a couple of balls....""",
438624: """XXX: hi! You online, can you talk?
YYY: Almost. Going to Sheremetyevo, I have a biztrip to Khabarovsk
YYY: Biztripe
YYY: Business trip
YYY: In short, I'm afraid of flying""",
438625: """In English "Xerox" is correctly read as "ziroks", and "xeon" as "zion".
Recently it dawned on me. Accordingly, the old series name "Xena: Warrior Princess", sounds. In reality - "Zina: Warrior Princess".
The well-forgotten old just sparkled with new colors...""",
438626: """xxx: We have a dude at work nicknamed "Ass Fairy".
xxx: Just for your information.
yyy: Now my imagination paints pictures of under what circumstances he could have gotten that nickname.
xxx: That was the plan.""",
438627: """Article about using scents in marketing:
"In a number of countries, primarily in Japan, scents are used to increase workers' labor productivity. ...When a woody scent is spread, a group of typists starts typing much faster and with fewer typos."

aaa: Only one smell can help you grind, the smell of money.
ббб: Well. Rubles are wooden after all.""",
438628: """Discussion in LJ
ххх: Chips are the perfect way to sell one potato for the price of a kilogram.
Answers:
yyy: Wine is the perfect way to sell a kilogram of grapes for the price of a hundredweight!
zzz: Sugar is the perfect way to sell a kilogram of beets for the price of a hundredweight!
kkk: A car is the perfect way to sell a ton of iron for the price of a wagon.
ddd: Perfume — sell a drop of wax and a spoon of alcohol for the price of a live sperm whale.""",
438629: """Dmitry: Where did I get to? Here in the parking lot near the building entrance there's a GTR :)
Oleg: you meant BTR?
Dmitry: Yeah. A Nissan BTR :)""",
438630: """In childhood the first stab in the back you get from the "Barbaris" lollipop, which lulls your vigilance with its unique taste and sweetness. And when you least expect betrayal, the candy cuts your tongue with the crack that formed in the process of sucking.
Welcome to the cruel adult world, sucker!""",
438631: """xxx: The internet is lagging, I don't have enough anger.
yyy: So for you the internet is a source of evil?
xxx: No! The internet is a source of goodness and knowledge, and the anger is caused by the provider's work )))""",
438632: """Lying on the couch, a cold, feeling awful. Son is playing PlayStation. I ask:
– Son, make me some tea.
He mumbles something indistinctly in response:
– Gonna die in a sec.
I don't understand at all:
– What are you saying?
He – clearly, with expression:
– Wait, gonna die in a sec and make you tea.""",
438633: """xxx: There's a very persistent feeling that lately people have way too much free time, with nowhere to put it except vegetating in social networks. It gets idiotic — the chief accountant sits late at work, always whining there's no time for anything, but her browser's start page is Odnoklassniki.
yyy: That's not "free time", it's escapism from reality, where you're a cog in the machine of senseless production and upselling.""",
438634: """Me: I hope it won't occur to him to switch the mode back and forth and dump this trash into one file
Him: Don't care, since I'll be the one responsible for this, I'll say it doesn't match the spec, everyone go to hell
Me: Anything can go to hell, but it must go to hell through the logs, not into the logs through the ass""",
438635: """As a kid I laughed at the cartoon where the little goat must count everyone, otherwise the boat will sink.
Now I work as a programmer and it's not funny at all.""",
438636: """xxx: You know, when a developer asks you what a boolean is — those are new, fresh sensations.
yyy: "-- Do you know what a Boolean is? Yes or no?!\"""",
}

if __name__ == "__main__":
    print(json.dumps(EN, ensure_ascii=False))
