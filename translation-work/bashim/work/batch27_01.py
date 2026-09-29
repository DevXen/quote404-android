#!/usr/bin/env python3
"""Translate bashim chunk_27 batch 1 (input lines 1-100). ru is read verbatim
from the input file; only en translations are embedded below."""
import json

EN = {
409075: """redis: oh no, was in a public restroom today, and on the doors as always there were writings of an entertaining-advertising nature
redis: one of them read "I want gay sex, boys, call me", phone number + a drawing of a dick, everything proper
redis: below it someone's cry of the soul: "Want sex - start renovating your apartment in December, bitch!!!"
redis: anyway, I've decided not to replace the balcony windows for now, just in case""",
409076: """derty: weren't you trying to lose weight?
No_NаmE: Yeah
derty: next week they're bringing another KAMAZ truck of bricks, wanna help?""",
409077: """Лисёна: wish it were veche already
MeloMan: let's gather the folk and the boyars...""",
409078: """3y6uk:
-Let's go for a walk
-Sorry, I can't...
-Why?
-I'm going to a wedding...
-Hmm, and after?
-After that I'll be married!""",
409079: """???: I asked Vadik to tell the dude who rented us the apartment that we won't be renting it anymore. So Vadik talked to him and tells me: "I told him we broke up and that's why we won't be renting this apartment anymore. And he said he knew all along we wouldn't last together, we'd break up. And his wife said the same, it seemed that way to her too. And remember - when we came to look at the apartment, there was a plumber laying tile? Well, he said the same thing too\"""",
409080: """forik77: hi, interested in the iPhone, can I spin it around and check it out?
4ikatilo: no problem, where do we meet?
forik77: considering your nick, let's meet somewhere crowded""",
409081: """xxx: Yesterday evening I almost became a stutterer after shining a flashlight under the hood and seeing two eyes staring right at me....
xxx: A kitty was warming itself up =)""",
409082: """xxx: Today a priest came to our school.
xxx: Apparently for the Orthodox religion lesson, to enlighten the kids.
xxx: Poor guy didn't expect the kids to chase him around screaming: "Buff me stamina!\"""",
409083: """xxx: My dad sincerely believes that Merry Cristmas is Ded Moroz's wife""",
409084: """xxx: Not believing in the 2012 apocalypse pays off.
xxx: If it doesn't happen, we'll laugh at those who believed.
xxx: And if it does, they won't get to laugh at us.""",
409085: """Страсная: why aren't you saying hi? did I add you just to stay silent?
Чарк-in: you know, at first I thought your nick had a typo in it, but when I looked at your photos I realized you also lisp""",
409086: """From LJ:
Khotilovo - Dobyvalovo - Kharchevnya - Bolshoye Opochivalovo.
four villages in order along the Tver-Saint Petersburg road
I think they bear witness to a most interesting journey of some early-medieval gopniks.""",
409087: """ЮРА(Zubek): congratulate me, I'm officially divorced 8-)
Viko: CONGRATULATIONS!!!!!!!!!!!!!
Viko: what are your next moves? ;)
ЮРА(Zubek): thinking of having some tea.""",
409088: """)Angelo4ek: too bad you're busy tomorrow.
)Angelo4ek: I'll be home alone, bored all alone :(((
ilia: you sure know how to manipulate men!""",
409089: """a friend of mine calls me and says she watched a funny movie yesterday, a Russian one.
I ask what movie, she doesn't remember the title, but starts describing it...
- well there's this dude who invented a time machine, and went back in time to Ivan Godunov.

at first I don't get it, then I'm laughing till I cry.
waiting for her report on watching a blockbuster with a broken arm and diamonds.""",
409090: """ххх: Kids conceived at the corporate party automatically become company employees""",
409091: """Andrey: so what do you do at night? )
Вячеслав: hanging out on Sanya's sites!
Andrey: moonlighting?
Вячеслав: no, spending, cap!
Andrey: don't spend too much.""",
409092: """Петруша: New topic on the forum:

- "looking for a doggy costume, can rent or buy, need it for 2 hours, anyone got one? for a height of 122 cm"

I start typing a reply:
- might have one, measure the chest girth, neck, and the distance from neck to tail.

And anyway, what kind of dog is 122 cm? and then I realize it's not a dog...""",
409093: """Walking around the mall with mom, I was looking for pants for myself. We go into one boutique, I see these awesome pants hanging there, I'm looking them over, feeling them up, then I glance at the price tag - holy mother of carrots! Expensive. But beautiful. I take them, head to the fitting room, and hear mom muttering under her breath "and God forbid they actually fit\"""",
409094: """Subway. Crush. A woman with a kid next to me:
Woman: Andryushenka, are you still alive?
Kid (gloomily): Yeah. One life left.""",
409095: """xxx: his neighborhood is dangerous, so he set up a camera in his hallway
xxx: obviously did it all himself, what security firms
xxx: good thing he set it up. comes home from work one day - only the wardrobes and sofas left
xxx: panic? no. gloating. thought he'd take the video to the cops, let them search
xxx: yeah. the video was recording to his computer
xxx: the computer they stole""",
409096: """Георгий: It's chaos at the firm. A drunk VIP client came to see the managers, treating them to cognac, playing guitar. At the assembly section the women are in a great mood - since morning they've been blasting Serdyuchka and Allegrova at high volume. At the mounting section the young slackers are working frantically to some hard club music. From my spot in the design office I can hear all 3 sound sources.
Георгий: Anyway I decided it didn't matter anymore and, exercising seniority, fulfilled a dream - turned on Grazhdanskaya Oborona in the design office.""",
409097: """A colleague told me. His friend goes to play billiards with his dad every week. And one time the kid was on a roll, just one ball left. His dad is sitting on the sofa, drinking whiskey, and tells his son: "if you sink this ball right now, I'll buy you a car."
The kid sinks the ball. Silence in the hall.
And dad's voice: "fuuuuck...\"""",
409098: """Rin: I've slept through renovations, screaming babies, karaoke, furniture being moved, a beginner drummer, drunk roleplayers.
Rin: I can sleep sitting, standing, on the move, in lines, while taking lecture notes or eating lunch.
Rin: I thought - I'd reached enlightenment. nope.
Rin: today my under-eye bags are the fault of my new watch. it ticks too loudly x(""",
409099: """Полина: I've got a bra, panties and a bow, and it's all so awesome... and he loves some Nastya""",
409100: """Снежана: I had a boss whose first wife was Tatyana, second wife Tatyana, two mistresses in Saratov named Tatyana, in SPb - Tatyana. the man simply wasn't interested in other names...

Chtulhu: so I take it nothing worked out between you and this boss?""",
409101: """xxx: you don't have a hammer drill by any chance?
xxx: my 25 kg bag of sugar has turned to stone
xxx: sick of chiseling at it with a spoon!""",
409102: """ххх: I've trained myself out of being rude
ххх: came over to some friends' place once. they have a one-room apartment
ххх: turned on the TV, flopped onto the couch with my feet up, next to the couch there's one of those cardboard beer packs, 12 cans, already opened on top. thinking, sweet, I'll grab a can
ххх: and then I hear the hostess's hysterical scream "Leeesh, there's no beer in there...."
ххх: *and my hand in slow motion sinks into the box*
ххх: "...that's where we put the used condoms!!"
ххх: since then I ask for permission""",
409103: """Inko: Bought some tea, excellent tea by the way. On the package there's a drawing of a teaspoon and the proud inscription - "Spoon included as a gift". I open it - and there's a FORK in there!!!""",
409104: """xxx: Heroes of Might and Magic. New hero: Putin. Special skill: when he gallops down a muddy road, it becomes paved in front of him.
yyy: And he also has a magic spell - "Presidential Motorcade" - all monsters on the map run away or give way
xxx: When he rides into a town, a grail is automatically built there. If you take him out of the town - the grail disappears :)""",
409105: """today at the lecture the prof burst in, 20 minutes late, in an ear-flap hat and felt boots all covered in snow, shouting: you'll fail my pass-fail anyway, let's go have a snowball fight! and ran off in an unknown direction""",
409106: """XXX: our department is so pretty - all decorated with New Year stuff.
YYY: and our boss banned us from "decking out" before the 29th, said: "no way, you'll start celebrating right away\"""",
409107: """xxx: the damn printer has been periodically printing the same document for 2 hours now, and nothing I do helps...
xxx: advise me, I've run out of my own ideas, maybe you'll spark a thought.
yyy: Well I have one universal piece of advice.
xxx. which?
yyy: Wash your hands before eating)
xxx. brilliant! I'll tell him that. ate paper with dirty hands, and now he's throwing up documents.""",
409108: """xxx: Used to be three aunties with abacuses sitting in accounting, and now it's ten people with LCD monitors and a frazzled sysadmin""",
409109: """М: Alright woman. Hold on
Ж: I'm not a woman, I'm a girl
М: Hold on anyway. Grab the tickle
Ж: I don't have a tickle, I can't hold onto it.""",
409110: """xxx: Why are you so interested in how many girls I've dated?
ххх: Wanna know how many I have left till the next level?""",
409111: """<NN_was> hello, physical persons :))
<polkan> get lost, legal ass.""",
409112: """kora_f: When a tsunami is coming, it doesn't really matter anymore where and how you run, imho.
Александр: Yeah, but those who run without soul and imagination won't make it onto YouTube.""",
409113: """ххх: In one of the hospital restrooms it's sweetly written 'to the demobilization like to the moon on a tractor'. Sadness. Waiting for leave.
ууу: The distance to the Moon averages 384,467 km; if the tractor goes at 45 km/h, it'll reach the Moon in 8,543 hours, which is approximately 355 days. So what's written in your shithouse is correct.""",
409114: """ххх: we could go to the cigar house
ххх: grab a cigar each, 100 grams of cognac, and vshakhmaty
ууу: how sweetly you veiled that!
ууу: "vshakhmaty"
ууу: me and ххх are at the cigar house, we're vshakhmaty""",
409115: """xxx: Never thought I'd remember with nostalgic tenderness the traffic cops who used to catch me speeding in the places where I now stand in hour-long traffic jams...""",
409116: """ххх: every day I drive home from work along the same route. and every day in the same spot the provider's internet drops. in the same spot. next to the cemetery )
yyy: dead zone""",
409117: """Marianna: Preved!
Marianna: Kagdila?
Marianna: 4e ne v univere?
Игорь: Mom, that's not fashionable anymore, especially not for you, a philologist.""",
409118: """ххх: When I sleep at work I keep dreaming about porn, what does that mean?
ууу: It means don't fucking sleep at work, the bosses will fuck you.""",
409119: """xxx: Damn, my table of contents looks like a list of Harry Potter books. ))
xxx: Mathematical Logic and AI Systems. Mathematical Logic and Modern Computers. Mathematical Logic and Programming.""",
409120: """Oren:
We're running a subscriber survey about service quality.
Me: hello, could you answer a few questions
Subscriber: eeeh... only if it's quick...
*I start asking questions*
meanwhile someone in the background: f**k, are you holding the ladder??!!!!
Subscriber: oops, gotta go!!!
*hangs up*""",
409121: """ххх: the printer was doing something for a long time just now
ххх: then it said it had refilled my cartridges for me
ххх: showed them to me full
ххх: warned me they're different from the originals
ххх: and solicitously asked whether it should continue
ххх: I didn't dare refuse""",
409122: """Wisp: You any good at diff eq?
Asket: I got an automatic pass.
Asket: So no xD""",
409123: """xxx: D'you know my funniest dialogue with clients? It repeated several times...

xxx: Layout handoff:
- This is going to be bad.
- How bad?
- Very bad.
- How "very"?
- F**king awful. Rare garbage. 100% defective. You can't look at it without tears.
- OK, print it.
- Sign the waiver.
- Fine (signs).
Print run handoff:
- (with hysterical notes in the voice) WHY didn't you tell us it would be THIS bad???!""",
409124: """A friend works as a trainer at a gym. Getting ready for a workout, I put on my gloves and walk into the gym. Still looking at my gloves I walk up to the trainer and ask: "Well, you brainless mountain of muscle, what am I doing today?". I raise my head and realize it's not my friend. I took the answer "get lost" as ambiguous.""",
409125: """Принцесса Анастасия: Will you come with me to Tron?""",
409126: """xxx: I'm increasingly convinced some people need to pass an exam for the right to operate a shopping cart""",
409127: """VaNiK:
Riding the escalator in the metro. Rush hour. The attendant screams over the loudspeaker: "Young man, where are you standing? Right at the escalator exit, move away!!!\"""",
409128: """A colleague at work is reading his daughter's letter to Ded Moroz, with a cosmetics catalog attached:
...give me #56123, #56155 and #56180 and please return the catalog after the holidays.""",
409129: """xxx: your life is what a normal person's life looks like in a Goblin dub!""",
409130: """mudakisa: Wanna see a thing?
lena: I'm an old woman, my general, I've seen things.""",
409131: """the_mockturtle: Remembered how a couple years ago at a seminar on Camus's "Caligula" someone casually remarked that Cherea is a name, not a gerund.""",
409132: """ххх: Sweetie, hi! I've been thinking, we need to rack our brains - your son's got his birthday in a month. what do you advise, as his own mother?
ууу: a good beating
xxx: wow. the boy's growing up )""",
409133: """Аваддон: How do you get a star down from the sky?
Demon: take a picture, or make a 3D model)
Аваддон: A model isn't the same.
Demon: then the choice is simple - controlled thermonuclear fusion!)
Аваддон: No problem! I just washed the pot.""",
409134: """From lor:
xxx: The double-click patent infringes on the earlier-issued patent on the single mouse click.
yyy: Twice!""",
409135: """Right now digging through someone's term paper. File list:

Kursach.pcb
Kursach_dorabotan.pcb
Kursach_dorabotan-konechnyi-variant.pcb
Kursach_dorabotan-konechnyi-variant-final.pcb
Kursach_dorabotan-konechnyi-variant-final2.pcb
Kursach_dorabotan-konechnyi-variant-final2_peredelan.pcb
Kursach_dorabotan-konechnyi-variant-final2_peredelan_etot-sdavat.pcb

Now waiting for the superfinal""",
409136: """Marochka: Are you Catholic?
DeadPoet: what makes you ask?
Marochka: Christmas is coming soon, should I congratulate you or not? )
DeadPoet: I'm an atheist
Marochka: So when do you celebrate Christmas?""",
409137: """svin: today walking down the street, two comrades unloading crates
svin: and using an interesting algorithm: the first takes bottles out of the first crate and puts them on the ground. the second takes the same bottles and puts them into the second crate
9_Xorn_9: warehouse defragmentation?""",
409138: """ххх: At 20:34 the movie cuts off!
ууу: Damn, really. Re-upload the movie please.
zzz: daaaamn, at such a moment! (((((((
admin: 20.34 - end of the episode.""",
409139: """zlot: yesterday at the smoke break we were sharing what we used to play, who was on Lineage, who on WoW, who on DotA. One little accountant just couldn't believe it, asks our respectable programmer - you played too? And he's like - yeah, L2, 3 years. Asks another programmer - and you played L2? him - no, I was on WoW, but quit already.. anyway, she polled two, three of them like that, got to Roma, and Roma's like our proper boy, dresses nicely and looks like a manager rather than a hardcore scripter-shitcoder
zlot: she asks him, did you play DotA? He says no. She - WoW? No. L2? No. She's all happy - wow, at least someone normal, batting her eyes at Roma
zlot: Roma, like a cowboy, flicks away his cigarette butt and goes - my internet was really shitty, I played Ragnarok, turned around and walked into the office
zlot: I thought the girl would cry...""",
409140: """xxx: Coming home from a party at 3 AM, saw girls with laptops in their hands, sitting on their knees in the snow.
xxx: Wi-Fi, I thought.
xxx: Yeah right, not that simple. AutoCAD with a building plan.
xxx: That's what exam season does to people.""",
409141: """xxx: and according to Dark Templars, people are divided into two halves - the upper and the lower.""",
409142: """хххх: This reminded me of how my friend went to saw down a Christmas tree last year. On foot. Picked, damn it, the biggest one - barely dragging it. Without a tarp. At night. Across a plowed field. There were like 3 more people with him. But their trees were smaller, and when headlights approached they lay down in the pits in the field. But with Sashka, no matter how you lie down, the tree sticks out. So he stood it up: a lonely Christmas tree growing in the middle of the field. Sashka hears: the car stopped, the engine was turned off. And someone's coming toward him. What he thought through, and how dry his pants stayed, history doesn't say. A man with a saw comes up to him and heads straight for his tree. And Sashka yelled at the top of his mighty throat: "OCCUPIED!!". The man vanished in an instant.""",
409143: """xxx: Mikhail Petrovich, we wish you enough money for the New Year!
zzz: And why "enough"?
xxx: So that you'd finally give us our salary with the bonuses already!!!""",
409144: """She: Imagine I'm your wife, what would you do?
­
he: get divorced""",
409145: """VIKA: Tell me if anyone knows. The cardiologist put in the report: Stage 1, Grade 1 hypertension, Risk 3. Blood pressure 150\\90. At the draft board they said it's borderline fit and sent me for a heart ultrasound, said if the heart's fine they'll take me to serve. But the cardiologist insists that with this report they shouldn't take me.
xxx: Tell them your forum nick. They definitely won't take you.""",
409146: """ХХХ: We were doing renovations in a hotel. An operating one. With guests. Anyway, I'm standing there, minding my own business, plastering a wall. Went to the john. Came back and observed the following scene. A drunk guest is plastering the wall with my trowel. Barely took it away from him.""",
409147: """A friend's status says: Which bitch made my voodoo doll and sings it lullabies after lunch?""",
409148: """Skripkina:
Walking home, on the street a granny was selling gutted chickens. The granny's phrase delighted me beyond measure: "Girl, buy a chicken! Look how beautiful it is, just like you!\"""",
409149: """X: Evil does not slumber!
Y: Oh, evil slumbers just fine, in the mornings...""",
409150: """Moscow officials were quite surprised to learn that the 6 billion euros allocated for building a railway in Italy had been fully spent on construction.""",
409151: """<xxx> tell me, is it comfortable to fly with a joystick?
<yyy> LOL
<yyy> try a broomstick""",
409152: """1: it'd be cool to make a video that could change the player frame by script when needed, or push the image beyond its borders
2: beyond the monitor's borders
3: that script would be called LSD""",
409153: """д: hey! you home?
п: yeah)
д: what location?""",
409154: """М: Hi. Bought the Christmas tree?
Ж: Bought =) Only we don't know how to set it up.
М: Green side up.""",
409155: """xxx: Preved! I bought a helicopter :)
yyy: what, is Moscow traffic that bad?""",
409156: """If there were no winter, and it were always summer -
We wouldn't roam so much through the wilds of the Internet
Haymaking, the river, the beach and babes would distract us
Site ratings wouldn't grow, if only, if only, if only!""",
409157: """xxx: My name is Yulia, I'm 24, looking for a guy 21-27 years of birth phone XXXX""",
409158: """I live in a small town, driving around town, traffic cops stop me:

- Sergeant XXX, your documents.
- (hand them over silently)
- Do you know your insurance expires the day after tomorrow?
- Yes.
- (returns the documents, salutes) Till the day after tomorrow.""",
409159: """<Oblako> I cobbled make.conf together out of whatever was lying around
<Oblako> and then I deleted whatever was lying around ;(""",
409160: """xxx: Thanks og...
yyy: You're welcome, troll!""",
409161: """xxx: I mean, I get that he's the boss.
xxx: But taking orders from a guy who not long ago chased you around the yard with a plastic pistol...""",
409162: """xxx: I wonder why they don't make real rum and whiskey in Russia?
yyy: apparently the sugarcane sprouts late - they can't harvest it in time for New Year's, and after that - seems like there's no need anymore""",
409163: """This morning they brought in a laser printer for repair, with a guitar string hanging out of it... Now we're pondering what's better to do with the printer - pull the string out, or add more strings and make a gusli out of it.""",
409164: """xxxx: What do you mean, I'm not sleeping with her. And never have!
xxxx: And not going to!
xxxx: And I can't even imagine a situation where I'd do that with her. I couldn't drink that much, even at today's corporate party!

... two days later ...

xxxx: You won't believe....""",
409165: """Олег: had a fight with my girlfriend, decided to set my VK status to "I'm a goat", like she'd see it, understand everything, we'd make up, but somehow it all went wrong. ALL my exes who were online confirmed it by liking it, and started discussing which other hoofed animal I resemble. needless to say, we didn't make up?""",
409166: """ххх: "Life is like a box of chocolates: you never know what filling you'll get."
ххх: Who slipped me the chocolates filled with shit?""",
409167: """xxx: So I asked for (in the store, not as a gift!) silicone lube - wanted to grease my "windshield wipers".
xxx: Now my "windshield wipers" have a delicate sensual strawberry aroma.""",
409168: """xxx: Mine came home at three in the morning from the corporate party
xxx: That's exactly what he said :)""",
409169: """On Saturday we met up with friends.
One told us how he and his wife went to a restaurant a week ago: "...a specially trained girl walks around the hall and with genuine joy asks all the visitors whether they've already decided where they'll celebrate New Year, and, well, invites them to celebrate at their restaurant. She comes up to us and asks with a smile like the Cheshire cat's:
- Have you decided where you'll celebrate New Year's?
- Yes, in the Canary Islands, Tenerife, Puerto de la Cruz
Then a Stanislavski pause, the girl's face immediately changes and very plaintively - "Take me with you\"""",
409170: """ЗайкОва: Assholes! I so wanted to enjoy my New Year depression and loneliness,
and you keep making me laugh and making me neigh like a horse,
my stomach already hurts from laughing. All my depression down the drain.""",
409171: """I get on the bus, walk in, hand over the money, and hear the driver telling someone getting in behind me:
- girl, why are you standing there? come on up! footie hop-hop, and you're in the bus! so? and the money? get your little wallet out and sprinkle some coins into uncle's little hand! *pause* oh, it's a boy?! no shit! kid, didn't you get enough sleep?""",
409172: """xxx: store
xxx: "Miss, why does this caviar cost 180 lei, and this one 210?"
xxx: "well, the pricier one is from Kamchatka, and this one's from Russia"
xxx: thanks to employers who only hire people with higher education!""",
409173: """xxx: got spam with the subject "Crap something useful under the tree!\"""",
409174: """/*
* Seryozha, you're a dumbass!!!!!!!!!!!!!!!!!!!!!!
* The fact that I laughed my ass off doesn't change the fact that you're going to burn in hell
* because thanks to you I was at work at half past ten on December 30th.
* */
//foreach ( var consType in consultationTypes )
//{
// throw new ServerErrorException( null );
//}""",
}

if __name__ == "__main__":
    import sys
    src = "/home/hatch/workspace/translation-work/bashim/chunks/chunk_27.jsonl"
    dst = "/home/hatch/workspace/translation-work/bashim/out/chunk_27.jsonl"
    start, end = 1, 100  # 1-based line numbers
    lines = open(src, encoding="utf-8").read().splitlines()
    assert len(lines) >= end, f"only {len(lines)} lines"
    with open(dst, "a", encoding="utf-8") as f:
        for ln in range(start, end + 1):
            r = json.loads(lines[ln - 1])
            en = EN.get(r["id"])
            assert en is not None, f"missing translation for id {r['id']} (line {ln})"
            assert en.count("\n") == r["text"].count("\n"), f"newline mismatch id {r['id']}"
            f.write(json.dumps({"id": r["id"], "ru": r["text"], "en": en}, ensure_ascii=False) + "\n")
    print(f"appended lines {start}-{end}, dst now {sum(1 for _ in open(dst, encoding='utf-8'))} lines")
