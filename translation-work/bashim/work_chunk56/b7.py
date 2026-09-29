#!/usr/bin/env python3
"""Translate bashim chunk_56 batch 7 (input lines 601-700, IDs 438737-438836)."""
import json

EN = {
438737: """Alexander:
I propose adding a priority "urgentimportantsoitburns", and when it's set the ticket automatically gets the status "rejected" with the note "tickets with such priority wording are not accepted".

Alexander:
In the same vein as the "call the director an asshole" button, pressing which locks the employee's account and pops up a window "you are fired for disrespecting the company's management""

me:
That's also a good idea, worth remembering.
Will come in handy for the helpdesk

Alexander:
Classic traps. Preferably with a confirmation dialog, so there's no "I pressed it by accident".
"Are you sure you want to call the director an asshole?\"""",
438738: """Ekaterina:
I'll never understand bank logic.
When filling out a consumer loan application I asked the manager whether I need to state the purpose of the loan, you know, like improving living conditions, buying an island in the Pacific, shopping in Paris. Would it affect the decision on the application?
No, no need to state it, won't affect anything, was the answer.
And three days later the bank's verification service called me and asked:
- What do you plan to spend the money on?
YES, I'M BUYING AN ISLAND!!!
They gave me the loan. Picking an island.""",
438739: """On pregnant women's tastes:
Brought my wife a flash drive with the movie "The Revenant" to the maternity hospital, she's watching it on a tablet.
- Don't bring me such scary movies anymore, I'm afraid to watch them.
- And what should I bring you?
- Something about vampires.
- But those are horror movies too.
- I don't believe in those, they're fun.""",
438740: """xxx: Writing my thesis. In parallel thinking about how to convince the committee that my thesis is good not only for pouring hot water over instant noodles, but also for starting a campfire, and in general it can be useful around the house.""",
438741: """T: How do you write the not-equal sign in Pascal?
S (Delighted she remembered first): Back-and-forth!""",
438742: """From a Moscow-region town forum:

XXX: Kids are growing up so early!!
Yesterday I'm picking up my kid from the NURSERY group of the kindergarten and on the door hangs this announcement about a parent meeting. The kids are 2-3 years old.

YYY: That's a talk about the parents. At the meeting the headmistress asked people not to come pick up kids from the kindergarten drunk and not to beat them on kindergarten grounds.""",
438743: """xxx: a report was walking through the forest
xxx: sees an sql exception lying there
xxx: sat into it and fell""",
438744: """<Senna> I saw an ad on Steam for a lumberjack simulator, remembered the driver simulator, the auto mechanic one, the janitor one
<Senna> and came up with a new game
<Senna> a C++ programmer simulator
<Senna> level one - hello world
<Senna> level one hundred - a C++ programmer simulator""",
438745: """On the post about F.E. Dzerzhinsky's "family nest":

sublieutenant: Loved the memorial plaque in the first photo - "Foundation of the house where F.E. Dzerzhinsky was born". Can't help thinking that Felix Edmundovich's birth was akin to the birth of a Chaos god, since it caused such destruction of the terrain""",
438746: """– Where do you work now?
– As an industrial fitness instructor
– WTF???
- As a foreman of loaders, basically""",
438747: """xxx: to log an emergency repair service, I have to define it in the warehouse and receive it in pieces! emergency repair, damn it, one piece!
xxx: what kind of moron wrote this program?
yyy: but you get to do an unscheduled inventory and yell at everyone around for a long time - "SHOW ME WHERE THE EMERGENCY REPAIR 1 PIECE IS LYING, DAMN IT, DECIDED TO SELL THE REPAIR ON THE SIDE?!?!?\"""",
438748: """xxx: I see you don't dust the tops of the wardrobes.
yyy: Those are decorations
yyy: cosmic dust
yyy: It's too high up, I don't notice it. The dusting trigger doesn't fire :-)
xxx: put it on cron.""",
438749: """- Should I ask him to take a screenshot?
- No, this doesn't call for a screenshot. This calls for scalping.""",
438750: """It was very imprudent to delete the Windows.old folder on the accountant's computer during cleanup. Who knew 1C was installed there :(""",
438751: """xxx: In the age of self-opening doors, a guy, trying to show gallantry, under-spun the turnstile in front of me.""",
438752: """The cat knocked the bathroom curtain down together with the rod. In the evening I'm showing this outrage to my husband.
Husband-to-cat: so what was that for?
Me: he wanted to get up to the curtain
Husband: and how? did it work?
Me: the curtain came down by itself...""",
438753: """To the "Walking Dead" screenwriters: the show's heroes build a big transparent wheel, stuff a dozen walkers into it, tie a baby goat or a piglet in front of the wheel, and get a perpetual motion machine! In the future you could create a civilization where the inexhaustible source of energy would be walking dead.""",
438754: """Pavel: took the cat to be neutered today. arrived, sitting in the queue, after about 15 minutes of waiting they take me and the cat into the office, ask what I need. I explain, they say ok and call a veterinary reproductologist.
anyway, I have a female cat named Snezhok.
Ivan: Pavel, next time I recommend you lift the tail and check if there are balls there)))
Pavel: Ivan, but I don't have a tail.""",
438755: """Had to go on business to an office located in a very non-obvious place - an old-development district, a house listed on one street but actually sitting off in the courtyards of another street, a semi-basement room, entrance from the courtyard... Plus the spring mud season, which also adds to the fun.

In general, the fact that the neighboring semi-basement turned out to house the Sports Orienteering Federation was somehow expected.""",
438756: """xxx: He was afraid to go to interviews, like they won't take me, I won't make it
xxx: moron, they already didn't take you, you're sitting on your ass!""",
438757: """xxx: there are the попаданцы into space opera, попаданцы into the past, попаданцы into anime and nearly to pink ponies, who rebuild the world into a "better" one. I wonder, have there been попаданцы to the Teletubbies yet?
yyy: No. Feel free to write about it.""",
438758: """xxx: In our courtyard lived a granny we called "Scooter" after the German band. And she got her nickname because she had a granddaughter Faina and the granny would go out on the balcony and yell across the whole neighborhood "FAAAYA...\"""",
438759: """xxx:
I remember when I was about five-six, granny used to stuff me with something she considered healthy and edible
I refused
she told me how they lived when she was little
if someone didn't want to eat and showed off at the table, grandpa would smack them on the forehead with a spoon, everyone immediately started eating and nobody ran from the table

I asked with childish directness
- and nobody stabbed grandpa with a fork for that?""",
438760: """dragonpetrovich: Litracy is one of the sines of inr harminy""",
438761: """Natusik: If I let you touch my hair - that means you're the chosen one.
Mikha: Depends where.
Dan_39: In the soup.""",
438762: """xxx:
The girls in our gym are so happy when they're called fitonyashas.
I wonder, has it occurred to them that "phyto-" means "plant"?
Not to mention that "nyasha" is "slime" :)
Girls, I think you're being trolled mercilessly )""",
438763: """yyy: Don't argue with them. They believe everything they can't understand.""",
438764: """From comments on the news about the MS chat bot
xxx: They haven't released their bot online in Russia yet. Here it would've already talked its way into a couple of criminal code articles :)""",
438765: """Father-in-law: So you really call each other Kisya and Zaya?!
Husband: We really do.
Father-in-law: Couldn't come up with anything more original?
Husband: We did, sometimes we answer to Krysya and Krotya...""",
438766: """xxx: We found a bug in the frontend
xxx: on one page there are three "save" buttons, one of which deletes everything
xxx: and the other two work differently""",
438767: """buka_jaz: At 1:30 AM you suddenly remember you have fresh chocolate pancakes with condensed milk, and your regular 10 km run authoritatively crushed the doubts. What can one little pancake do against a woman devoted to sports?!""",
438768: """xxx: Vanya, I love you very much, it's just that sometimes there's no time at all, even to eat or go to the bathroom.
yyy: I love you too. Too bad we see each other so rarely, even though we live together.""",
438769: """Val: I want to hop to Smolensk on the commuter train.
Kir: why? Napoleon already got his ass kicked there.
anthony_hawk: Napoleon got his ass kicked in lots of places, so what, just sit at home now?""",
438770: """From search_again's LJ:
Yandex Direct advertised diamonds at me for a long time, and now it's pistols. Apparently so I'd have something to buy the diamonds with.""",
438771: """Last day of annual reporting, submitted everything, some already by mail, got back around 11 PM. sitting with my husband, I'm eating, he's watching TV:
H: What crap they're showing on Russia!
I glance at the screen and want to correct him that it's not the Russia channel, it's Channel One:
- It's 1C!
H: Go to bed!""",
438772: """The director is a man of average height and build, his deputy is a two-meter huge hulk. The secretary (shared by both) is a small skinny woman. In general, they brew tea themselves, because the kettle is stored on the top shelf, Lena can only reach it if she puts a stool under.""",
438773: """Mom, thoughtfully looking at the TV, where the central channel is showing a clip about Louboutins and amazing pants:
- In my youth a song would become popular, and then someone would make up dirty lyrics to that tune and sing it with a guitar in the kitchen. Now they remove the swear words from a popular song so it can be played on TV.""",
438774: """xxx: I can't block the ad-blocker-blocker ad with another blocker... what is this called?
yyy: Corporate collusion?""",
438775: """Alexander: Can't do this without an intro. My eldest son is Artyom, he's 19. He lives with his mother and her new family. The mother's husband is named Sasha - just so happened we're namesakes. Their little son, also, damn it - Sasha.
He called yesterday to ask about his new little brother:
- What did you name your brother?
- Yan.
- Oh thank god...
- What did you want?
- Anything, as long as it's not Sasha.""",
438776: """<portnov> I've got a test case number 1984 here, needed to create a person for it
<portnov> what do you think I named him.""",
438777: """From Habr:

gag_fenix
Read up on System Z to broaden your horizons. I once wrote a paper on them.
It's a system where (at least) everything is duplicated: 2 pipelines inside the processor core, reserve cores, reserve processors themselves, hot automatic memory replacement — all in a module that's also replaceable. When replacing/upgrading many hardware components (e.g. the processor) no reboot is needed.
And what other system has 30 years of backward compatibility?

webmascon
in the bills.""",
438778: """xxx: Sean Bean will be killed in the Final Fantasy XV movie.
yyy: Bekmambetov should invite him into the Lukyanenko movie.
yyy: For the role of Semetsky.""",
438779: """xxx: Folks, read the news here. So what, you can run KDE, GNOME, Unity on Windows?
yyy: KDE or GNOME is one thing, but Wine would be powerful.
xxx: That's exactly "why?"
yyy: So they don't say Windows is worse than Linux because Wine doesn't run there.""",
438780: """xxx: From the fact that "per the experiment's condition" (a thought one) we retrospectively reconstructed the simplest proto-eye that can possibly exist in principle. If even it has some predecessors - they are NOT functional, bring their bearers no benefit.
yyy: Numerous owners of photoreceptive systems simpler than your "proto-eye" are looking at you in bewilderment.""",
438781: """Had a dream last week:
Driving a car in the city, alone. Driving terribly, accidents don't happen only because the people around manage to react, not me - to do something. Coming off another bridge onto the embankment, almost driving into oncoming traffic, I brake, but manage to scare a driver, he swerves and almost hits a car in the next lane.
And then I realize I'm not in a cozy iron box - a car, but on a bicycle, and the oncoming traffic is 3/4 dinosaurs. HUNGRY.
That's all you need to know about my attitude to driving school and the upcoming exams.""",
438782: """Internet provider support after a conversation with an angry subscriber.

xxx: We now have a group "Dolboklyui" in the monitoring system
yyy: =)
xxx: The main thing is, when we provide statistics, don't show clients the name of this group. I'll rename it to VIP, I guess""",
438783: """JustAJuice: We were setting up a router for a client. Dictating the IP: well 192.168... A voice from behind: "Sixty is not spelled like that". And that's when we realized it was the finish. The first client in my practice who wrote an IP in letters into the address bar.""",
438784: """About life in the USA, "Bay area girls" forum (San Francisco and surroundings):
XXX: Girls, does anyone know - what's happening in Walnut Creek? Huge lines of people standing downtown (near California Pizza), some sitting on bags.. Look a lot like refugees...
YYY: That's the line for the new Tesla 3 preorder...""",
438785: """xxx: my eldest son's girlfriend is American. The younger one just has a hard time with languages and reads Russian poorly. Brought them to Moscow. Walked around the city. The girlfriend and the younger one would hang under every sign and street name and read for a long, long time. In revenge I took them to Sharikopodshipnikovskaya.""",
438786: """xxx: well you know thoughts are material, and you just need to really want it
yyy: I know. Off the top of my head I can name at least 5 examples of my big wishes coming true
yyy: only in this matrix something apparently short-circuited, and MY wishes come true for OTHER people!!!!
yyy: the first time I noticed it was at uni. My friend got exactly what I'd been dreaming about, down to the details. I'm not a very envious person, but I was in shock, especially that the details matched.
yyy: Then there were other fulfillments of my dreams happening to relatives and acquaintances. Don't know if they were happy, after all the dreams were originally mine, but the details, the details matched so suspiciously
yyy: Just recently my dream came true for my neighbor in the building, whom I barely talk to
yyy: it's not for nothing they say: be careful what you wish for, sometimes they come true. They just don't specify when and for whom. Wanted to put a smiley and don't know which, laugh or cry...""",
438787: """xxx: our school was elite and all, but we still acted like little shits with the teachers. Like in any other school. How they tolerated it - I just can't imagine. Still ashamed. But our OBZh teacher, a retired colonel, owned us like nobody else. He found a way to stop us cheating on tests. We wrote all tests in gas masks. You could only take it off when handing in the paper. Took it off early - retake. And it's really funny - when the whole class sits in gas masks. And if you start laughing - the lenses fog up, you can't even see your own paper, let alone a cheat sheet. And if you start fidgeting for no reason, it's super obvious because you bang that respirator box against the desk or your neighbor. All in all the guy was great, left very vivid memories.""",
438788: """Yana: Don't yell at me. You're not my husband!
gatto: I'm not a husband. I'm worse.
gatto: A husband can always be sweet-talked, begged, motivated, poisoned as a last resort.
gatto: But that trick won't work with me.""",
438789: """xxx: "Obdolbysh" is written without a soft sign at the end, right?
xxx: Masculine gender
yyy: +
zzz: Writing a tech spec?
xxx: Yeah, in the notes""",
438790: """xxx: Mom finally ripped the carpet off the wall (installing AC), and there... A hugest empire of moths with its own hierarchy, primitive legislation and budding culture.""",
438791: """We have something like a dacha - a house in the suburbs that belonged to my great-grandfather. After his death the house was divided among his four children (my grandpa and his siblings). All the heirs already have children, grandchildren, and some even great-grandchildren, so there are constantly some people appearing there whom you remember by face and name, but who exactly they are to you off the top of your head you can't figure out. Personally I was there yesterday for the first time in, probably, a couple of years - brought mom.

In general, midday. The grandma's brother's relatives (son, GRANDSON, two granddaughters and someone else) are getting ready for lunch. Can't recognize one of them, whispering I ask mom - like, who's the young man in glasses and a blue shirt? Getting the answer:

-That's Olya's boyfriend... Or Katya's... Anyway, don't rush to memorize, he might not be a relative yet!""",
438792: """xxx: Tomorrow at 7-22 I'll be in Guangzhou Baiyun International... phew, how do they even sing songs here???""",
438793: """thyl: "Kepler-10 b is very close to its star, has a very short orbital period around its star, and a very high surface temperature. Unique is the very high density of the planet: it is 8.8 g/cm3, which is higher than the density of iron, so the planet is assumed to be iron. The illuminated side of the planet is most likely covered with a molten metal ocean."
thyl: no water, no vegetation
Alexander: Inhabited by metalheads""",
438794: """Telling the client's rep the estimate. This here, that there (can't go into detail), summing up the interim total:
- This is already...
And then I glitch:
- ... twenty long khokhars.
The client's rep opened his mouth, closed it, opened it again and with understanding:
- Got it, sorta...""",
438795: """XXX: Tale of "Diman and the revisions"
.....
For the third time he threw in the revisions..
The revisions arrived with the final mockup..
XXX: In the end Diman must stay with the draft mockup""",
438796: """[01.04 13:20] Lara: Great. My daughter called, congratulated me on my professional holiday.""",
438797: """xxx: And how do your weekdays go?
yyy: Observing postmodern. And the staple diet of the average Rasiyanin. Borscht, a TV series with Vladislav Galkin, vodka and a garage ;)
xxx: Where'd you get a garage?
yyy: Won it in cards)""",
438798: """Discussion "Cattail vs reed"
ZZZ: Whoever rustles is the reed!
XXX: that's far from certain. Under the window the local drunks are rustling, but they're not reed at all, they're firewood.
ZZZ: Well in the sense of "The reed rustled, the trees bent..."
XXX: Well, they sometimes rustle so hard the trees bend, yeah )))))""",
438799: """On professional deformation. I remember working in the export department of a big plant. We sold pipes there. I translated our Soviet GOSTs into English for the Chinese, so they'd make things per our GOSTs. One big contract for big money dragged us into a long work bender with no days off. I watched a movie then somehow. The movie was "House of Wax". A female colleague asks how I liked the movie, I say the movie is crap, but one plus - they kill Paris Hilton cool there. She's impaled on a pipe there. The colleague asked "what pipe?". Without blinking an eye I answered "seamless austenitic ten". Then a pause and roaring laughter.""",
438800: """in telegram:
m0nk: damn :) love digging through analytics — turns out, despite the bad CTR, the second ad's conversion is higher.
the_zoi: my watch just showed the preview of this phrase of yours VERY badly right now!""",
438801: """xxx: The spectators never noticed the demonstration performance of professional ninjas.""",
438802: """amatorero: Today I fought off the cat and cleaned up all the junk on my computer. I'm straight up two-twelfths Hercules :D""",
438803: """FiXablom: Who is this guy?
serbun: herostratus
Anonymous: author, why'd you remind us, as if anyone forgot him""",
438804: """xxx:
Our lecturer complains. Doesn't like all these internets and googles: "You look for an answer to something debatable, and in the end they cite your own works, in which you started doubting and that's why you went to the internet in the first place".""",
438805: """xxx: I have no dirty socks at all...
xxx: I just give them to women, then magic, then I take them out of the drawer.""",
438806: """zzz's knee stopped cracking by itself, and the need for surgery went away

xxx: congrats on the knee =)
xxx: it will serve you faithfully and truly, till death do you part
zzz: Well thanks, of course. But I'd hope to die not the kind of death that parts me from my knee""",
438807: """We only come to the dacha on Sundays, six days we're away. As soon as we appear on the doorstep, a hungry stray cat comes. We call him Je Ne Mange Pas Six Jours""",
438808: """The eternal debate married vs singles. An acquaintance, 30 years old (wife, 2-year-old daughter) tells about his unmarried friend:
- Well once he lived with a girl, five years, she started dripping on his brain about marriage, he dumped her. Since then he lives alone. Why he needs a relationship, he doesn't know. He's more comfortable drinking beer every evening and watching football.
- That's how natural selection works.
- (looks at daughter) Yeah. Well that's harsh.
- But true.""",
438809: """XXX: thought about it, why would I pay for karaoke where I'll suck, when I can pay less and do vocals with a teacher! Per minute it's definitely cheaper""",
438810: """xxx: looks like your Photoshop ran out of paint. Actually that'd be a good idea for Adobe on how to punish pirates.""",
438811: """My husband went to his granddaughter's carnival, which the preschoolers' dance studio put on for the kids. All girls from 3 to 5 years old, naturally, princesses. They lined up all the "princesses" in a row and ask each one:
- what's your name?
- Barbie etc. (each princess also has a fairytale name).
They come up to a girl of about 4
- And what kind of princess are you?
And the girl loudly into the microphone - NORMAL.""",
438812: """Having sex with my husband.
Me: Yes! Hurt me!
Husband: Well... I can give you a flick on the forehead...""",
438813: """A man comes up, asks how to get to Such-and-such street, and it's not that far, but there's no direct way. You have to go up onto the bridge, cross it, go down, pass under the same bridge etc. In general, explaining takes almost longer than actually walking.

Having listened to the explanation, he asks:

- Or maybe forget it... Girl, is there a mobile phone store anywhere closer?!""",
438814: """Snegurochka: If you have Linux, you're no longer alone.""",
438815: """111: I was in this business, don't tell me fairy tales. The exchange owners thought the same when their cash units were smashed with hammers. Every glass has its purpose, it goes through certification. says bulletproof - means it holds a shot from a certain distance from a specific weapon (depending on the class).
222: "when their cash units were smashed with hammers" - right now I respect you twice as much!
333: "I was in this business" - did they start making glass too strong and the business went bust?
444: how much did the hammers go for?""",
438816: """Humanist: Misha, at the end of each statement you need to write: "...in my opinion..." or "...it seems to me..."
skygoo: Is that your opinion that Misha should write like that, or does it seem that way to you?""",
438817: """xxx: How's young life? )
yyy: Passing... (""",
438818: """The future is near.

"A rice cooker knows 2450 cooking modes for 200 rice brands. Mode selection is automatic — just photograph the barcode on the package with your smartphone camera and transfer this data over a wireless connection using the appropriate app."

I feel the joke about "getting root on your microwave" will soon become reality.""",
438819: """xxx: men cry too. For example, when after long work on yii2 they ask you to fix up an old project on yii""",
438820: """<Angelofnet> Red: A real Indian won't criticize someone else's quipu for uneven handwriting!""",
438821: """From a car forum:

xxx: knocking in the engine after replacing the valve stem seals. Help figure out the causes and what the consequences could be
yyy: there can be many causes, and the consequences can be different.
xxx: Thanks""",
438822: """I'm lying on my girlfriend. And then she says - "honey, I think there are three of us now". I turn around and see the cat zealously humping her leg.""",
438823: """xxx: In the smoking room we figured that to lift a ton of cargo to the height of the sun, with constant near-earth gravity, you'd need to use the energy of atomic fusion of 4 kg of a tritium and deuterium mix
yyy: Can I ask? Why?
xxx: You can't
xxx: We don't know((""",
438824: """xxx: in Belarus on average there are only 30 sunny days a year. That's 1/7 of a year
yyy: a Venusian year, or are days 42 hours long?""",
438825: """yyy: went to get a new tooth filed down
xxx: at least something's free with them
yyy: ???
xxx: the tooth's under warranty
yyy: and where's the warranty?
xxx: in the medical record
yyy: and if you move to another city
xxx: I'll take the record with me, they have branches everywhere
yyy: hmm
yyy: paid a lot?
yyy: for the tooth
xxx: NNNNN rubles
zzz: holy shit!!! at these prices soon you'll be buying teeth on the "secondary market" from classifieds
xxx: yeah. "selling a tooth, used, under warranty, serviced by a dealer, service book included\"""",
438826: """yyy: Maybe moonlight as a taxi driver?
xxx: Here taxi drivers periodically go missing. Or get found somewhere in the forest belts.
yyy: Well you've got a navigator though)""",
438827: """yyy: on your video the sound lags
xxx: that's how it should be. the video is on faraway European servers, and as we all know from the example of a thunderstorm - image (light) travels faster than speech (sound)...""",
438828: """xxx: Got chickenpox at 32. If my laptop had face-recognition login, I definitely wouldn't have been able to log in...""",
438829: """doing a banner mockup for a construction company on a freelance assignment. get this email:
"regarding the construction mockup they wrote a wish.
Could you write HOUSE CONSTRUCTION diagonally across the banner in transparent paint?\"""",
438830: """Listening to all these stories about debt collectors I keep being tormented by one question. If I pay my loan on time, can I come to the bank and write on their wall something like "Ivanov from apartment 7 pays on time!"?""",
438831: """xxx: I wonder what the fear of changing something is called, if it's not in version control?
yyy: rationality
xxx: And if it's the kid's diaper?
yyy: git push trashcan feature-pampers""",
438832: """Xxx: I've got the sads
Xxx: I went to the radio store to buy an Arduino and a display
Xxx: And someone already bought the display
Xxx: Bought the Arduino
Xxx: Now it's lying headless in my pocket whimpering
Xxx: God, what am I even saying""",
438833: """xxx: It's not easy to rattle a Petersburg intellectual...
UUU: Yeeeaah right))) Curb, building entrance, shawarma!!!""",
438834: """sitting laughing like a horse, yesterday gave the regions the source of one softinka to compile and deploy, so to speak, on their servers. giving the source because many have all kinds of motley linuxes. so in the morning I get a ticket - your softina supposedly doesn't work. I dig in. and there's an IT guy there, a smartass of smartasses, he decided to look at the code first. saw the line #define OPTIONS_FILE "/etc/billing.conf", decided that # is a comment, and that I forgot to remove it, so this smartass "uncommented" the line""",
438835: """On the news that Whatsapp is implementing end-to-end encryption:

xxx: Our whole internet is full of this nonsense. Still unknown how it'll actually work

yyy: The Economist (a serious British publication) once wrote that for total secrecy, you need to talk at night, naked, in the middle of a field during a thunderstorm. And even then it's not certain your conversation partner won't rat you out...

xxx: I can picture the next James Bond movie...""",
438836: """From the discussion of an article about the strongest solar flares capable of knocking out electronics, satellites and disrupting power supply.
xxx: I feel a magnetic storm on the sun a day before it starts on earth.
Is this the gift of prophecy??..
yyy: a request to you, please post a day before what the article predicts, so I can stock up on diesel fuel without the hype.""",
}

if __name__ == "__main__":
    print(json.dumps(EN, ensure_ascii=False))
