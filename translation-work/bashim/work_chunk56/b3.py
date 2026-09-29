#!/usr/bin/env python3
"""Translate bashim chunk_56 batch 3 (input lines 201-300, IDs 438337-438436)."""
import json

EN = {
438337: """... actually, computer games with well-written dialogues can develop speech no worse than books, especially if a person has a developed auditory memory. For example, I know of a case where a person unlearned saying "ложить" only thanks to the game World of Warcraft, in which you quite often hear a character complaining that there's nowhere to put things.""",
438338: """xxx: I don't like going shopping, especially annoying is the need to talk to salespeople. So I always spent very little money on my wardrobe, could wear one thing for years.
xxx: And then I found Korean sites...""",
438339: """XXX: When I was trying on lenses at the eye doctor, I go "wow, everything's so sharp :)"
YYY: And I remember, when they picked glasses for me, I even crouched down. That's how sharp everything was.""",
438340: """xxx: I've lived separately from my parents for 15 years already and see my little bro less than once a year, but his name still automatically bursts out of me as a reaction to any disaster, instead of swearing. Even if I hit my finger with a hammer...""",
438341: """xxx: Remember, you went to the store for potatoes?
yyy: Yeah. What, did they all go bad?
xxx: No, they're all beets. Go again.""",
438342: """kika:
How hard our ancestors had it! Won't let you play soccer, will take away the bike, the soldering iron, the "Young Chemist" kit, no going to Dima's birthday, no cartoons, basically a total pain.
And we have it easy - one phrase: "okay, Liza, iPhone on the table immediately".""",
438343: """Discussing the site:
No time to read? We offer summaries — only valuable ideas from the most useful books.
No random books. And no "water" in them either
30 minutes instead of 5 hours of reading

s0no: Brilliant idea, I reckon. The content of most detective stories, thrillers and other heroic fantasy can be laid out as follows: "He <имя главного героя> came, saw, conquered".

Having read this comment, you can confidently tell those around you that you've gotten through no less than half the books written in the history of mankind)).

Dramas and other love stories are laid out with a different phrase: "Came, suffered, stopped suffering". Or just died)).

congregatio: ... and the Japanese version - "everyone died" :)""",
438344: """<Copyel>: Like they're doing it. Well if so, it's a sign, I'll lie down and nap)))
<ChertoffPes>: Did you tell them to hurry up?
<Copyel>: No, I decided it's a sign and I need to sleep
<ChertoffPes>: Listen, I come to work every day, and there are signs all around that I should sleep.
<Copyel>: Listen to the signs.
<Copyel>: Sleep.
<Copyel>: Learn to sleep with your eyes open.
<ChertoffPes>: The problem is the management isn't superstitious at all and doesn't believe in signs.""",
438345: """xxx: What happens if a werewolf bites a centaur or a minotaur?
yyy: A horse)
xxx: Or a wolfhorse?
zzz: A wolfhorse or a bullwolf, yeah
aaa: The question was about biting, not fucking, I thought. Why do you have such weird versions?""",
438346: """My husband's work has been a total "F..." since January. All days off taken away, completely, half a Sunday can in no way count as a day off. Added hours to the workday on a voluntary-compulsory basis. Promised "it's only until the end of March... maybe..."
Called from work today, with tears in his voice asked me to pick him up. Said he'll be good, kind and affectionate. Will go to bed without tantrums, eat his porridge and go to his mother-in-law's village to chop firewood...""",
438347: """I'm a translator. Not a technical one, actually. But they threw me a text about marketing strategies, illustrated with examples of IT corporations and their product sales. And the examples start from way back, so there's equipment whose name isn't in every dictionary, and even then it's not always immediately clear what it is.

In short, March 8th. Middle of the day. I call a techie friend with the question: "Sanya, what is (name of a video system, obsolete and out of circulation since the late 80s)?".

In the receiver you can hear Sanya scratching the back of his head with all five. Finally a counter-question:

- Are you practicing necromancy over there?!""",
438348: """xxx: "and consequently we rule out the Netherlands, Holland and the like"
xxx: the notorious "and the like" especially intrigued me. Flanders, probably.""",
438349: """Platoon: they say there are more deaths in a leap year
LmD_ex3151: Absolutely right. About 1/365 more""",
438350: """from helpdesk tickets:

Please connect the accounting employees XXXXXX X.X. YYYYY Y.Y to the vacuum""",
438351: """Geektimes:
At home, in the days of DOS and win3.1 there was very little chance to copy a game onto a floppy, had to dig around the system, but I never once killed that very DOS and win3.1, because my parents clearly said nothing can be deleted except the Games folder.""",
438352: """xxx:
Sometimes our grandmas in their care for us reach incredible absurdity.
Evening. The apartment is dark, the light is on only in the kitchen where I'm cooking something. I step out of the kitchen – the hallway is dark. From around the corner some inhuman roar: "AAAAH!!!".
I'm surprised I didn't scream back, by inertia I take a step – around the corner stands grandma (she was heading to the kitchen). Stammering I ask: Why are you roaring?! She: well it's dark here, I see you're coming, I was afraid to scare you... o_O""",
438353: """From Avantyura:

A: The margin and competence are fine, it's just that the people coming to the bank for loans now are not at all the ones the bank would like to lend to. Just my observation..
B: By my observations the bank is eagerly ready to lend only to those who don't give a damn about the loan.
A: From a conversation with a security service rep of one big bank this followed absolutely.""",
438354: """David Blaine's video about his record: How I held my breath for 17 minutes.

xxx: at the train station in a public toilet I held it for longer than that!""",
438355: """Iron Thumbelina: Tell me, are all you men prideful to the point of stupidity?
Jackie Pan: what's up with you again? The guys at the gym bugging you again?))))
Iron Thumbelina: No, dad pulled one.
Jackie Pan: well, spill )))
Iron Thumbelina: I was going to take the dumbbells, the barbell, plates and other stuff to the gym so they wouldn't be in the way in the room, I only train at the gym anyway. Two duffel bags about 70 kg each. Whatever, just carry them to the elevator and then load them into the car. But then my dad floats out of the room. You remember him?
Jackie Pan: yeah I remember, a big healthy guy
Iron Thumbelina: Yeah, about 120 kilos of weight, but since I was born he hasn't lifted anything heavier than a tea mug. He doesn't need to, of course, but he comes out with the words: "Sweetie, you're a girl, let a man help you!"
Iron Thumbelina: Damn, yeah, I'm a girl, but I've got almost 70 kg of lean muscle at 160 cm tall and I've been benching 100 for ages!
Iron Thumbelina: Anyway, I didn't even get to object. He grabs those duffel bags, one jerk... Anyway, he not only didn't lift them, but tore his knee and threw out his back... Had to, as a warm-up, carry his carcass in my arms to the bed like a baby and call an ambulance...
Jackie Pan: brutal...
Iron Thumbelina: So. Are you all like that?
Jackie Pan: yeah)))))""",
438356: """xxx: Sent the client a letter by fax, calling back to clarify the incoming number, asking who received the fax. 
xxx: An indignant answer from Cap: "the fax was received by Fax!!!\"""",
438357: """xxx: our new orderly is great
ххх: doing a deep clean of the ward, I tell her: we need to wash the walls and ceiling
ххх: she goes: why?
ххх: I go: well damn, bacteria settle there, then they fly to the patients, we've already got the whole set from staph to pseudomonas and klebsiella
ххх: her answer killed me: no, YOU'RE WRONG, bacteria only settle on the floor, so the floor needs washing, but there CAN'T BE bacteria on the walls, they're sterile(!)
ххх: yeah right, bacteria don't have wings, they can only crawl, what was I thinking....""",
438358: """xxx> A group of scientists from McGill University created a fundamentally new type of DNA helix - from 3 strands instead of the usual 2 - using a substance that's used in pools for disinfection.
yyy> Skeptics shamed: with proper use, window-washer fluid is omnipotent!""",
438359: """about the wrong sensations
a young lad, soaping himself up to be my lover, was feeding me, when I asked if he'd bought condoms, that condoms are bad for a man and everything feels totally different in one
I, an inquisitive girl, asked, different how?
he answered "like in a felt boot"
I got a laughing fit, barely squeezed through tears: why were you fucking a gnome's felt boot?

didn't work out with us, all for the best""",
438360: """XXXXX > The director at the morning congratulation: "Lately our organization has been striving toward the feminine principle". Though then he corrected himself that he meant the M/F ratio.""",
438361: """ххх: Secrets always come out. But by then almost nobody cares anymore.""",
438362: """Ilee: I got out of the bath and quickly walked to the comp, and IT is crawling after me and HISSING!
Ilee: turns out I can jump almost straight from the hallway to the balcony
Ilee: then it turned out I'd caught a cellophane bag with my foot, and the air stream from the closing door gave it extra momentum
Ilee: how strong instincts are though - I could see IT perfectly with my inner eye, IT was horrible!""",
438363: """ххх: so, how do you like Africa?
ууу: First impression - in a 40-square-meter room there's not a single outlet! Wi-Fi there is, TV there is, outlets - no.
ууу: No problem! They don't know Russians!
ууу: unscrewed one of the 5 light bulbs, got to the socket, attached a wire to it, wound a splitter onto the wire, and into the splitter went the laptop and chargers...""",
438364: """Carter: Constantly hear kids running around in the apartment above, but I live in a building entrance with only 8 apartments and I've never seen kids in my entrance
Skripka: Maybe you just get up late and come home late?
Skripka: Or they have Alf living with them""",
438365: """News from geektimes:
Nvidia managed to install a full version of the flagship GeForce GTX 980 into laptops.

Comment
realscorp: Now you need to coat the screen and keyboard with Teflon — and you'll get an excellent portable waffle iron.""",
438366: """yyy: You often shoot in backlight. How come you most often shoot against the light?
xхx: Since childhood I've always been against everyone. Against rules, against imposed stereotypes. When I picked up a camera, someone told me - don't shoot against the light. Since then I've been shooting like that - against the light. And in general, I'm against light.""",
438367: """Adding to the collection about meter-pushers - use it.

xxx: You urgently need to change your water meter blah-blah-blah
yyy: no need, thanks
xxx: but you'll pay by the communal one!
yyy: oh no we won't!
xxx: ???!
yyy: we'll be RECEIVING by the communal one because you're calling the WATER UTILITY'S ACCOUNTING DEPARTMENT!""",
438368: """xxx: asked a question on mail.ru on the topic "how to quickly get rid of mice that showed up in the apartment?". Among other suggestions received an answer brilliant in its humanity and simplicity: "Just don't kill them, it's a sin. Make sure they don't multiply".
xxx: wanted to whiten my karma, hopelessly stained by sophisticated murders of flies, mosquitoes and even one wasp. Sitting and thinking where to start, with a lecture on the dangers of unprotected sex or free condom distribution?""",
438369: """Ххх: and Zoroastrian fire mages can also touch red-hot iron and not get burned

Ххх: even yogis can't do that

Ууу: my grandma flips cutlets with her fingers in the frying pan and moves pots on the stove with bare hands

Ууу: she's our Zoroastrian mage of the 32nd level, info 146%""",
438370: """xxx: consciousness dies along with the stopping of chemical processes
yyy: What happens to your consciousness after death?
zzz: yyy, when a computer dies what happens to its OS?
yyy: I believe it reincarnates into another machine, gets installed again and again, updating anew, and dies anew.
zzz: And remembering nothing of its past installations
yyy: Thanks, you made me feel like a serial killer""",
438371: """<Eladeria> A dialogue between Andrey and Timofey :
A: Did you congratulate grandma?
T: Yeeah
A: And what did you wish her?
T: Good night!""",
438372: """Sad kot: The day has come when my phone has more RAM than my computer.""",
438373: """Underscore:
Irish bus roulette. Try to guess whether this 46a goes where Google Maps and your eyes say, or stops 2 km from the destination and says that's its last stop

Underscore:
Although maybe I misunderstood the word last, the driver looked old""",
438374: """If you swear reading your two-year-old code, it means you're evolving.""",
438375: """Clients I can understand, but colleagues too have the brains to send me March 8th congrats by email. Me! By email! Every goddamn day I answer 100-150 emails! The best gift for me is an "empty" inbox!!!""",
438376: """work chat:
- Friends, which of you speaks Polish?
- If the client speaks English, our team will gladly chat with them:)
- Your team is already gladly chatting with them!""",
438377: """- Damn, Anka is some kind of chthonic horror! I ask her - what do you want for March 8th? She: "Oh, that's very easy. Strip." Before I could get happy, she adds: "Pose for me for a couple hours. I haven't drawn nude anatomy in ages".
- And I tell everyone my brother is dating a troll woman. Though people often misunderstand me.""",
438378: """Comments on the news about a fire from kids playing with matches:

PPPV: Somehow, if it's kids, it's "playing with matches", if adults set the fire, it's "careless handling of fire"... 5-7-year-olds already need to be taught to handle matches so games don't end like that. You need to give the kid a box of matches and ask him to light a match, hold it in his hand, put it out... And repeat several times. First, the kid won't immediately drop the match as soon as it flares up, and second, in a tough situation he'll be able to start a fire.

drozofil: Well, they did start a fire.""",
438379: """xxx:
Was sitting working at home. Went to the kitchen to eat. Meanwhile mom was vacuuming/dusting in my room. I come back - Ubuntu greets me! I reboot, choose Windows in grub - Windows greets me, the one I had a YEAR(!) ago. Climbed into the BIOS, chose the right hard drive as the first boot device - everything went back to normal. QUESTION: how can you, with a light flick of a rag, reboot the comp, enter the BIOS, change the boot device priority, exit it with saving? No cat could ever do that... It was scary, honestly. ))""",
438380: """Read all sorts of fitonyashkas and puffplushkas and decided to find out what my ideal weight should be...

Found a collection of different methods online and started counting. By one I've got 7 extra kilos, by another - the same amount missing, by the third everything's fine, the fourth prescribes me to eat no more than 1200 calories a day, the fifth - no less than 1400.

Sympathized with both warring women's camps, closed it with a light heart.""",
438381: """xxx:
my ex came to visit
was unhappy with how my teenage son answered his questions
voiced a complaint
- the son is badly brought up
I sighed and said with a tear in my voice

- well he's an orphan, growing up without a father""",
438382: """xxx: Babe, did you feed the rats yesterday?
yyy: Mmm, no.
xxx: And the day before?
yyy: ...mmmmmmm. No-o.
xxx: Hm.
yyy: So, let's check, maybe we have fewer than two of them now...""",
438383: """Frigo: back in the day they didn't bother with pretty titles and people flocked to the cinema as they should
Frigo: Huang Fei Hong yu gui jiao qi (Hong Kong)/The Magnificent Kick
Frigo: no deception
Frigo: from the title you immediately know what you're going to watch
Alex: honest guys
Alex: not like all these "knockin' on heaven's door"
Alex: what they'll knock with is unclear
Alex: with a foot
Alex: with a hand
Alex: dunno""",
438384: """xxx: On the road there are two types of cars that can make the drivers around them behave decently and carefully: a new Porsche and an old KAMAZ.""",
438385: """In the "Society of Skeptics" group they ask: tell us how you became skeptics?

xхx: In 10th grade my parents told me Santa Claus doesn't exist, but even then I had a skeptical mindset and didn't take their word for it. But by my 3rd year, under the weight of education, I came to believe in his nonexistence.""",
438386: """xxx: This insult can only be washed away with blood!
yyy: Sure can. And what will you wash the blood off with?""",
438387: """XXX: in the morning I was getting ready for work in some kind of daze. Came to work. Went to accounting, had tea, went to the director twice, and only by lunch did a slipper fall out of my pant leg. A HOUSE SLIPPER, CARL!!! No, I understand you can leave home with your tights sticking out, but to leave with a slipper...""",
438388: """Ххх: We usually ferment two jars of cabbage. Finish them - buy new cabbage and ferment more. It doesn't over-ferment, and there's always some. Very convenient.

Ууу: And we don't ferment at all at first. After a while we look - none left, as if we finished it. After that we don't ferment again. And so all winter. Also very convenient.""",
438389: """xxx: Young people drove into the yard, cranked up the music and started dancing. Making noise, won't let anyone sleep, midnight. People started leaning out of windows and yelling bloody murder. I look out the window, a burly guy silently comes out of the building entrance, climbs into the car, opens the hood, rips out the battery and just as silently leaves with it. Curtain.""",
438390: """ххх: decided the other day to cook tasty meat with dark beer and black bread. Bought a bottle, but when it came to cooking, accidentally drank it. Next time prudently bought two bottles. Without noticing polished off both. Yesterday took four, sensibly reasoning that I wouldn't drink two liters of beer in passing. Guess what happened? I'll become an alcoholic faster than I'll get to eat meat.
ууу: you're moving in geometric progression! and you said you learned nothing from the school math course.""",
438391: """Lived in a dorm in one room with an Indian guy. Then he left, they moved in a Russian. Well what can I say? Less privacy - before I could swear in my own language, the neighbor in his. And now...""",
438392: """I'm sick, my ears are stuffed, so I hear worse than usual. My husband is telling me about his work stuff:

- And on Wednesday at nine in the morning we have a scrum meeting.

I ask again:

- What what? A shitty meeting?!

He said that's the most accurate description of this form of planning meeting he's ever heard...""",
438393: """My kids and I get dressed for horseback riding at home. Completely - breeches, boots, helmets, luckily the stable is 10 minutes away. So I'm driving two kids to their lesson, and to walk my horse myself, everyone's properly dressed and equipped, a traffic cop stops us. I, without thinking twice, roll down the window, he looks inside... Then at me. Then inside again. And says: "You are a very responsible mother. I've seen everything, child seats up to 15 years old, even double seat belts, but a whole family driving in a car in helmets - that's a first!\"""",
438394: """Comment on a geektimes post about a new patch for Diablo II almost 16 years after the game's release:

FoxCanFly: Now THAT's real LTS, not your Debians.""",
438395: """xxx: hello Natalya
yyy: hello, I'm not Natalya
xxx: then who are you?
yyy: and who are you?
xxx: I work at an organization (blah blah about the stock exchange) I have information that you're interested in stock trading
yyy: you also have information that I'm Natalya
xxx: goodbye""",
438396: """As a kid I watched the cartoon "Ghostbusters", in which the hunters had a device for finding those very ghosts. And in almost every episode it "went off the scale". Could never understand why they wouldn't repair it / remake it / switch it so it showed normally.
- Vasya, what's the voltage in the network?
- THE MULTIMETER IS OFF THE SCALE!!!
- Then why the hell did you set it to 200 volts? Put it on 750.""",
438397: """xxx: trouble came to us in the face of the management""",
438398: """Sitting reading, the TV mutters in the background and suddenly I got interested in what I heard from an annoying ad: "...thanks to "Horse Power" shampoo - hair grows right before your eyes". I imagined with horror a lady with hair-overgrown eyes. Slept badly...""",
438399: """From Geektimes - discussing the crash of a Google car that cut off a bus:
- In short — Google basically wanted to cut it off, but didn't make it. Now we have to wait for Google taxi with checkers, roadside and a special detachable robot "Who were you honking at? Where were you even going?"
- In the trunk there should be a Boston Dynamics robot with a bat at the ready.""",
438400: """- Yeah, closing this project is like slaughtering the cow that lays golden eggs!
- The cow? God, what else don't I know about development standards in this outfit? Actually no, don't tell me, I'm afraid I'll like the answer even less.""",
438401: """Scarecrow: Here's our street in spring, on one side solid ice, on the other ankle-deep water. Where's it safer to walk? On the water. But! Under the water there's the same ice. That's how an ordinary street demonstrates the Zen essence of our world - choose or don't choose, everywhere it's all the same and all is vanity.""",
438402: """Came home from work, tired and hungry. My wife says "I didn't cook anything, make something yourself and eat", wife's on vacation. I go to the kitchen, make fried eggs with tomatoes. Ate, sitting watching TV. My wife says "You've gotten totally brazen, you ate and I'm hungry\"""",
438403: """After college I lived a couple years in a dorm for young specialists at the design bureau where I worked (it was the mid-2000s). The design bureau, importantly, is located in a not-so-near suburb, entertainment options - the dorm library, weekend dances at the local community center, drinking and working. Since the library was small and lasted at most half a year, not every body could handle that much drinking, and the dances were scary because of the very harsh local gopniks, only work remained. And the design bureau is a secure facility, and without an ordered pass you can't get inside on weekends, the boss's scariest threat was: "I won't let you into work on weekends!\".""",
438404: """zzz: There's a certain charm in finishing old saves in computer strategy games. You look at it and think: hmm, with what goal did I colonize this planet? Why did I declare war on two neighbors? What is this fleet doing in this ass?
Why did I build 16 dreadnoughts and name each one "Laser Joe"? ;)""",
438405: """... you just don't know how I got hooked on console games. My dad, when I was 5-6, he played horror games at night with mom in MY ROOM while I was trying to fall asleep.
And when dad bought TWO memory cards at once, dad and I fought over who'd play first :D""",
438406: """xx: The hosting doesn't have "tech support", just "support". Time and again talking to them, I solve the problem myself and tell them how I did it.
yy: Yeah, just "Support".
yy: Like, don't worry, you'll manage
yy: Try haaard, come on
xx: yeah yeah xD""",
438407: """From Habr:
xxx: I'll note that the idea "the average user is an idiot" is generally correct, so the browser makers' position isn't that groundless.
yyy: but they make a browser convenient ONLY for idiots!""",
438408: """tender_pantera: What are the proportions of blini?
pushist1y: circumference to diameter - about 3 to 1""",
438409: """mr. proud > Are you offended?
Asy > moving on..
mr. proud > I love these feminine "little things", I can just picture sex with a girl. She's like:
- Honey, not there..
- What?
- Not there!.
- What do you mean?
- Moving on!
- Huh?
- Oh... All!!!
Asy > I won't sleep with you
mr. proud > Uh... Ok. Deal =)""",
438410: """xxx: And when did the Soviet Union collapse?
yyy: At the end of '91
xxx: In December?
yyy: No, earlier...
xxx: And the Decembrist uprising is about something else, right???""",
438411: """I'm not married, approaching 30. With a friend just like me, for fun we're sitting on a dating site. Looking at candidates.
P: Well this one's okay. But his smile is kinda crooked.
Me: Hey, this one's normal.
P: No, his ears are different.
Me: One left, one right?
P: Yeah. But this one's cute.
Me: No, his background leaves are too green.
P: Agreed.
The leaves are too green. LEAVES. TOO. GREEN.
So how am I supposed to get married here?""",
438412: """Suddenly realized that if, as part of import substitution, they write a Russian STL mandatory for government agencies, then in priority_queue they'll make a constant JUST_ASKING defined as infinity.
Need to sit down and write it.""",
438413: """- Honey, how did you like tonight?
- Good, but it would've been better if I didn't have to fight the script...
- Then submit to it!""",
438414: """"Engaged in glycogen resynthesis". Such an answer to the question "What are you doing?" sounds way more scientific and solid than "Drinking sweet tea with waffles and gingerbread after a fifty-kilometer run". Immediately clear that the person isn't just lying on the couch, stretching out their run-tired legs, but is engaged in serious business. ))""",
438415: """Zet:
not an office but a fishbowl
walking to the toilet, someone's coming out the opposite way too
by the time I approach the toilet he's already so close you can see the whites of his eyes
I go in, close
2 seconds
KNOCK KNOCK KNOCK
I want to jump out screaming DO YOU THINK I'M LEAVING FOR HOME FROM HERE!?!?!?

Darya:
watched too much Harry Potter
that's how you get into the Ministry of Magic""",
438416: """yyy: what don't you like about the popup window?
хxх: usually, what pops up isn't the most useful product.""",
438417: """xxx: Actually they're more convenient to eat with chopsticks
xxx: There are chopsticks for suckers
xxx: They're fastened together
yyy: Those are nunchucks though.""",
438418: """OpenStack> where to buy the book "Effective system administration while intoxicated"?""",
438419: """xxx: And what do you need to work with? If with the head or the penis, I'd agree to a long-term contract. If with hands or feet, then sorry...
yyy: You need to punch holes in walls for laying pipes and cables. Can you do that with your head or penis?""",
438420: """xxx:
resume title: "Looking for work (only real work with the living)"
xxx:
what the person meant - unclear...
yyy:
what if you send them to the morgue? Or somewhere worse
xxx:
indeed, all funeral shops are fighting over the sales consultant, how could you not guess""",
438421: """Came across while reading reviews of a shockproof cap for babies:
xxx: Product matches the description, arrived quickly 26 days from ordering, put it on my wife's head gave her a fist, she said it holds the hit, not worried about the kid!! all OK!!""",
438422: """yyy: Well overall yes, Amazon is magnificent. Just take Amazon Fresh — once we ordered groceries, and among all the groceries one lemon didn't arrive (yes yes, one lemon). So after complaints to support they refunded me $0.40 and put $35 on my account for the inconvenience… Class!
xxx: If I got stiffed on a lemon, I'd be outraged too.""",
438423: """AV: For storing backups you can use any disks provided there are at least two of those disks.""",
438424: """One of the comments on the news of Bo Andersson leaving AvtoVAZ:
xxx: Told you - the place is cursed!""",
438425: """(From a forum dedicated to paranormal phenomena, esoterics, magic and other similar topics)

ххх: in our family everyone's a hereditary witch. And I'm the strongest, even cats give way to me.
ууу: have you won millions in the lottery yet? When you do, then we'll talk about your great gift.
ххх: you don't understand what you're laughing at! You don't know the rules of using the Force!
ххх: me not know? I'm the rarest witch! Ask my husband. Whenever we fight he always says he married a real rare witch!""",
438426: """Dear ChFpR delivers:
ххх: Inspired by a post about fanfics. Our "content policy specialist" quit (copywriter in PR HR-speak). Looking for a new employee for 3 weeks now. The boss lady told me to interview one, since I used to work as a you-know-copywriter.
A dude comes to us: lacquered mohawkish bangs, sweatshirt, trekking sneakers... Only thing missing is a beard.
"I'm creative, I'm positive, I'm a millennial... Startup, coworking, fuckup, motivation, digital integration..."
Ok, I say, write a dictation. And thinking to myself: "Why would I need you, hipster." I've seen plenty of them, they're as much workers as an elephant is a jeweler.
Him: "Oh, why don't you use online dictations?"
That's exactly why, to catch ones like you.
PrEzEntation, of the fUture, got iT...
Then, when the startuper left, we were howling over the dictation. The proofreader was howling with relief.
Oh yeah. He graduated from MSU.""",
438427: """xxx: Yeah right, I'll watch Spider-Man and believe that a bite from some crap will make me a superhero?
yyy: Definitely! Just in another world, and in another universe
zzz: Why so cruel? He can totally become a super-crap right here.""",
438428: """A friend accidentally gave herself away, pouring tea for her rarely-visiting lover:

P (looking at the signet on his finger): Oh, didn't you have a ring with a black stone... (thoughtfully) Or wasn't it you?""",
438429: """raila_sai: Where to get 5 more hours a day?
Xing_aka_kelos: start saying "norm" instead of "normalno" and "go" instead of "poydyom"!""",
438430: """xxx: They have such a close family. They do everything together. Not like us.
yyy: Exactly. Their family reminds me of playing four-handed. 
yyy: On the balalaika.""",
438431: """Spring came. Parents with kids play on the playground intended for dog walking (with a fence). And why? There's less shit there...""",
438432: """Calling the courier service to place an order.
Me: city of Saint Petersburg, Nevsky Prospekt, 48, need a courier for tomorrow.
Girl: I don't have such a prospekt in my database in Saint Petersburg.
Me: O.o how come?
G: I'm telling you again, there's no such prospekt in Saint Petersburg!
Me: How come there's no Nevsky Prospekt in Saint Petersburg?
G (getting angry): here in my database under Nef- there's only "Neftyanaya Road"!!!""",
438433: """News on 4pda: "Apple patents a "Home" button made of liquid metal" Comments:
xxx: t1000
yyy: Good old t800 will still take him down.
xxx: if it doesn't lag on the new firmware
zzz: good old 3310 will tear them all apart)""",
438434: """xxx: My grandpa was a radio amateur. Now he's an alcoholic.
yyy: They say it's not mandatory. Although, they say, yes, it's desirable.""",
438435: """xxx: I don't use power armor in Fallout
yyy: why not?
xxx: the sensations aren't the same...""",
438436: """xxx: And I don't want autocorrect, then words like "vaasche", "senya", "tamushta" can't be written
xxx: "Nizya" either
xxx: And "ischo\"""",
}

if __name__ == "__main__":
    print(json.dumps(EN, ensure_ascii=False))
