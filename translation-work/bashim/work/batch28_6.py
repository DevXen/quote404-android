"""Chunk 28 batch 6: records 501-600 (ids 410579-410678). Appends to out/chunk_28.jsonl."""
import json, os

EN = {
410579: """@gr00sh: Today I bit my female, she said she'll now also turn into a moron.""",
410580: """Corporate chat:
manager: Yoshkin cat! Not a single organ has been made yet! Feels like they put their organ on our order.
director: No worries! Slavik will come soon, quickly find use for their organs till death. I already transferred all the necessary energy to him by applying an organ to the brain.
admin: So what the [censored!] did I configure the anti-profanity for?""",
410581: """блонди: actually my phrases never caused a big reaction, never became catchphrases. until I went to driving school...""",
410582: """xxx: stop turning me against men, I still have to sleep with them""",
410583: """xxx: which algebra textbook is more reliable, Alimov or Makarychev?
yyy: dunno, why do you need it?
xxx: from Alimov: Any number to the zero power gives one. From Makarychev: Zero to any power equals zero
yyy: and?
xxx: I need to compute 0 to the power of 0. Which algebra textbook is more reliable?""",
410584: """Nastya:
A guy from the housing office came to replace the central heating riser.
Fussed around for a long time. Our cat Fedor came to see what's up. And suddenly the master started talking to the cat. "What are you doing sitting at home in March? Not good! Come on, I'll show you attics and basements. I know all the hot spots here!\"""",
410585: """from diary.ru:

standing here all in my favorite AC/DC t-shirt, washing dishes and quietly humming a Mikhail Krug song under my nose.
dad, thoughtfully sipping kefir:
- Something's bothering me, can't figure out what...""",
410586: """xxx: since I went to first grade, I haven't gotten enough sleep""",
410587: """Office. Monday. Sitting gloomily looking out the window, the girls are drinking tea:
- Zhen, join us for tea with cake?
- Don't want anything sweet.
- What do you want?
- Semi-sweet...""",
410588: """xxx: In our ward yesterday three gave birth: 2 girls and a boy
yyy: O_O
xxx: I mean, 2 girls and a boy were BORN.
xxx: But they gave birth the old-fashioned way, only girls )""",
410589: """Bloodyhell: Hi. ) What are you doing?
Deathtofalsemetal: Hi ) writing a manual for myself.
Bloodyhell: why?
Deathtofalsemetal: Dima responds to all complaints: "How was I supposed to know you can't do/say that? No manual came with you." He'll have a manual now.""",
410590: """Sh00t: she'll give birth to triplets for you and you'll buy a minivan
dervishdance: no. then I'll buy a minigun. be sure.""",
410591: """xxx: go to hell everyone, I'm a bird of high flight!
yyy: yeah, a woodpecker in the stratosphere...""",
410592: """ххх: Dad announced it's time for me to prepare for independent student life, in particular - learn to cook.
ууу: And?...
ххх: Good news: I mastered a new dish - salmon in cream and white wine. Bad news: seems there's no university in Russia with matching scholarships.""",
410593: """xxx: Hot water was turned off due to heating main tests.
xxx: Judging by the huge puddle of hot water near the house, the tests failed miserably...""",
410594: """xxx: there was a case - a classmate decided to skip a class, rode to the kinotsentralny, about to transfer to another bus, meets the teacher whose class is now and to the question: "Are you also going to the university, Ivan?", answers "Yes", sits in the bus with the teacher and rides back to the university.""",
410595: """Bikers often hang out near my house. Yesterday walking home from work I saw this scene: two bikers standing... well, one of them standing, and the other kneading a gopnik. Pinned him to the wall nice and neat, holding him by the clothes on his chest with his left hand.
Biker: *smack in the mug*
Gopnik: My homies will take you out so bad you won't know what hit you
Biker: *smack in the mug*
Gopnik: Alright no beef! let's split even, no nerves!
Biker: *smack in the mug*
Gopnik: You want I'll give you a grand myself
Biker, swinging his huge fist for another hit, turns to his friend: "Hey, this looks like switching channels!\"""",
410596: """ххх: yesterday I'm sitting in an internet club on an anonymous dating site, chatted about random stuff, she writes me: "want a candy? =)"
ххх: I answer: "Sure =)"
ххх: the girl to my right stands up, walks over to me, puts down a candy, goes back to her seat...""",
410597: """xxx: no, I'm definitively afraid to talk to Nadka online
xxx: my body can't digest it when she writes me "be there in 5 minutes, my suuunshine <3"
xxx: and five minutes later kicks the door in with the words "so what the fuck did you want, asshole?\"""",
410598: """xxx: Before throwing shit at grandmas who've overgrown their apartments with junk over their whole lives, clean up your terabyte drive, student.""",
410599: """Фекла Сигизмундовна: Any woman who helped a man find a pair for a sock knows that men distinguish a huge number of shades of black: 'black', 'I'm telling you, black', 'other black' (and its shade 'YES OTHER BLACK, DAMN'), and the most favorite color 'Don't give a fuck anymore, give me this one'""",
410600: """sablin522: Why does everyone, absolutely everyone, when they learn that I'm building a steam engine at home, casting aluminum and slowly making a milling machine, advise me to get married?!""",
410601: """xxx: Become ordinary - stand out from the crowd of individuals!""",
410602: """xxx: An acquaintance got in a car accident, disputed situation, gave the traffic cops the flash drive from the dashcam, sealed in an envelope.
xxx: The cops studied it carefully - found him not guilty in the accident, and along the way fined him many thousands for 4 incidents recorded on the flash drive and in the end revoked his license for driving into oncoming traffic recorded at the start of the day.""",
410603: """xxx: a friend works in the MIA, told how they brought a gastarbeiter to the station and started checking his registration
xxx: not only was it handwritten (should be fully printed), it was also issued by a department in Khuevo-Kukuevo.""",
410604: """ksennus:
A friend told me:
Her acquaintance (or relative, don't remember exactly), lives a few entrances over from her. Once he called a taxi. Just drove off, the friend called him on the phone. As a result his plans fell through and he needed to go to her place. So he asked the taxi driver to drop him at her entrance. He looked at the acquaintance like an idiot, but drove him.
A couple days later the friend also called a taxi. Gets in the car:
-Hello. Sorry, it turned out I need to go just a tiny bit...
-It's fine, miss. I drove some idiot here the other day, he needed to go practically to the next entrance!
-Well we're going to him right now...""",
410605: """ххх: Hi young daddy! How's it going?
ууу: Pretty good... But I don't call my daughter sunshine anymore.
ххх: Why?
ууу: Because the sunshine SLEEPS at night!""",
410606: """The cat's in heat. Loudly meowing under the door. Sister (a biology student) circles around, singing a homemade song about "the kitty who wants a tomcat". Opens the door to the entrance and... the neighbors who just stepped onto the landing instead of "Hello!" hear (sing-song):
- I'm an animal! My eggs have matured!""",
410607: """xxx: Any more photos?
yyy: no
xxx: Thank god!""",
410608: """Miracle: why so short?
­RankoR: an algorithm is not a dick
­RankoR: the shorter the better""",
410609: """AnjyJo: Want pills for laziness (
Chamie: Only have suppositories
Chamie: Pepper ones""",
410610: """111: Vanek, you've probably already уеал?
222: Mom, please clarify which keys aren't working for you""",
410611: """ххх: and my brother, when he was getting his military ID, only had tractor rights, and they wrote, I quote, "tractorist of tractors\"""",
410612: """My realtor tells me: a girl and a guy came to her office to formalize a deal. I quote: "And her guy is kinda (*makes a wrist-turning motion slightly above the temple*)... a bit of a programmer. Started asking for food during the contract signing\".""",
410613: """Shulz: I live in a country unsurpassed in the degree of its virtuoso dumbfuckery. An ordinary five-story-building yard. In autumn some kind soul from the administration allocated budget money, and the yard was improved - they made fences so people wouldn't drive onto the flower beds. And the snowfalls this winter were severe. The fences disappeared into snowdrifts. Well the yard needs cleaning, at least in late February. For this purpose they ordered a tractor, again at budget expense.
Which demolished them to fuck.""",
410614: """xxx: Read on a forum: "Who knows? Is it possible to buy a bulletproof vest! and where? up to 18 kg. to clarify right away For running!"
xxx: We live in dangerous times...""",
410615: """In class, the teacher is a retired military man, talking about holidays, someone asks about how holidays work in the army.
Teacher: Well how, festive ration, they'll give two candies and two cookies.
Question from the audience: And are the candies chocolate?
Teacher: What, do you want to leave the army without pants!?""",
410616: """sssnapeman: I live with especially sick people in a dorm for imbeciles.
sssnapeman: you gotta be something to think of poisoning cockroaches with a burning tire... WITH THE COMMANDANT'S APPROVAL.""",
410617: """xxx: hi
xxx: don't you want to meet me or have a drink
xxx: don't want, no
yyy: at least put question marks, self-taught jedi.""",
410618: """xxx: Be careful using pliers to open uncracked pistachios... The kill radius of sharpened pistachio shrapnel is about three meters. x_-""",
410619: """Funnare: We were prepared from childhood for the fact that the world rests on social inequality, that's why the second joystick on the dendy had no Start and Select buttons""",
410620: """xxx: Nothing makes a girl look younger like caps lock turned on.""",
410621: """ххх: Don't rush much - probably our metro line will stop now.
ууу: Why?
ххх: I was riding from the site - carrying a couple of dead switches in a bag, a bunch of wires and cables. Dozed off. Basically, I left it in the car.
ххх: A peacefully lying bag in the corner, with some black boxes and colorful wires sticking out of it...""",
410622: """@koffboy: It's good to be a cat: stretched - everyone goes aww, rolled from one side to the other - everyone goes aww, gained 5 kg - everyone goes aww again.""",
410623: """xxx: What can I use to wipe mounting foam off my fingers?
xxx: Fuck...
xxx: And off the keyboard (""",
410624: """* Xomyachok has joined channel
<Xomyachok> hi everyone, does anyone here breed hamsters? =)
<mike> breed you?""",
410625: """xxx: just now was, excuse me, in the toilet at our work
xxx: on the wall hangs a sheet with printed jokes, and the jokes are the dumbest, in the style of a free newspaper
xxx: and on top someone stuck a photo of a mollusk and a printed inscription - Wall newspaper "Uncle Plankton's Jokes\"""",
410626: """koroko: an Indian came to us. In our language he knows: 'yes', 'no', 'love', 'signature', 'mine', 'yours' and 'putin'. How am I supposed to explain the essence of the contract to him?
ДрелЕмоск: puzzled ) teach him the word 'if'. show the document and say: 'mine yours love if yours signature, if yours not signature, yours not love mine, yours not love putin'.""",
410627: """news: Deputies decided to oblige Russians to snitch on relatives

comment:
- Hello Prosecutor's office?
- Yes, I'm listening.
- My dad bought a Bentley with stolen money!
- Masha?
- Dad?""",
410628: """Mom told me. A neighbor with her daughter (7 years old) once dropped by for tea.

Neighbor: My daughter has started being afraid of something lately, constantly asks to sleep with me, I have to send my husband to the kids' room.

Daughter: Yeah right! I'm not afraid of anything! I just feel sorry for you! I heard and saw how dad tortures you at night, you scream so loud! You could at least say thank you.

Haven't seen the neighbor in a while, apparently the husband totally exhausted her)""",
410629: """xxx: were reading riddles at work during the break.
xxx: "two brothers see each other but never meet"
xxx: the accountant, thoughtfully from behind the computer: debit and credit...""",
410630: """xxx: But I did go to Pelevin )
yyy: where to?
yyy: into WoW or something?""",
410631: """from useless_faq

lageos: And if a werewolf bites a satyr, centaur or minotaur?
neonohoret: The front half of the centaur at full moon will try to munch the back half.""",
410632: """sanzh: citizen Vladimir Vladimirovich Medvedev blew the minds of the passport office workers.""",
410633: """xxx: I'm going gray...
xxx: fuck, how can you think to say "the server flew" meaning it started working faster?""",
410634: """xxx: How sexy the vampire chicks are in modern movies. Such pretty cover girls. Only one thing breaks my brain.
xxx: How do these bitches manage to do such hair and makeup when they don't reflect in the mirror?""",
410635: """Негодяйк@: our accountants at the last job quacked too )
Негодяйк@: the whole team laughed for a week, and Vanka the admin just snorted in hysterics
Milky: why did they quack?
Milky: were they getting fucked at work?
Негодяйк@: nooo. Vanka was on a bender, wasn't coming to work. He explained to the accountants step by step over the phone how to install some program. When it came to cracks, they asked HOW IS THAT??? so he said, like ducks quack, you do the same... So they quacked
Негодяйк@: thought it was a ritual...
Негодяйк@: probably shouldn't have told them about tambourines back in the day...""",
410636: """Lizi: What's the name of the disease when you feel like everyone around only wants sex from you?
_ВеcенняЯ_ДевочкА_: megalomania""",
410637: """umka: my friend doesn't believe in god at all, and instead of just saying: "God keep you", says: "May the instinct of self-preservation keep you\"""",
410638: """uksusss:
Oh, we laughed so hard in the cafeteria just now. Two cleaning ladies are scrubbing the floor with mops, and hockey is on TV above them.
Xbox kinect 2.0 in action.""",
410639: """On the sidewalk lies a flattened pigeon, a mom with a small child walks by.
The child got interested in the pigeon, looked at mom questioningly.
Mom: It's just a pigeon that shed its winter coat and flew away!""",
410640: """xxx: Russian instant roads - just add water >_<""",
410641: """ххх:
"How wretched their look and how wild their attire! Are you living creatures? Can you be asked? Answer, if speech is given to you!"
ууу:
Alvir, shooting a zombie crowd with a shotgun while quoting Macbeth over Skype is already too much!""",
410642: """ellustrator: In Sweden you can drive an hour on the highway and meet no one but deer.
christos: Same in Moscow.
persivalus: and in Kiev too
tuatlan: you can drive an hour on any highway all over Russia and meet no one but deer.""",
410643: """xxx: An amazing trend lately to have inflated demands while having only a pussy.""",
410644: """xxx: What I love Universal Studios for is the logo, the globe. It's round. You can immediately tell if the picture is stretched vertically.""",
410645: """Space: If you think about it, revenge is okroshka""",
410646: """Economics lecture at dentists'
Lecturer: Where to get money? You can sell goods. And what can you sell? (hinting at services)
Students, after thinking: A KIDNEY!!!""",
410647: """xxx: Megafon payment cards!
xxx: 15 years on the guitar pick market!""",
410648: """xxx: Print me 50 color pages with diagrams please, you have a color printer at work.
yyy: Don't want to
xxx: What, will they scold you?
yyy: They won't scold, I'm the CEO there. Just don't want to.""",
410649: """semiurg: Saw artificial willow branches in the store. And eggs — beautiful, plastic, with ХВ written on them. Ritualism identical to natural. And religious-feeling enhancer E256, approved by the Ministry of Health. Glutamate of Christianity.""",
410650: """xxx: It's good to work in an adult store!
xxx: You can kick the assortment all day on legal grounds!""",
410651: """xxx: Experience studying calculus suggests that you should fear most the topics with the word "Elementary" in the title.""",
410652: """Kiset: Recently bought a globe for my kid. While paying, the thought flashed: won't it become outdated in two years?..""",
410653: """xxx: In former USSR countries the presidential elections are most often won by the candidate "who would have doubted.""",
410654: """Окси:
8 AM. Intercom rings. Dragging myself to the door, hitting everything in my path:
-Yes?
A pleasant polite baritone:
-Hello, my name is Ivan. What do you think, why do people do evil?
-Because they wake people up early Sunday morning with the intercom >:-(
-Uh... Sorry.....
Ivan was lucky he rang the intercom and not the apartment door :D""",
410655: """From a motorboaters forum.
xxx:
Had to sleep right in the boat under the open sky, luckily it was still warm and no precipitation.
In the morning was woken by someone's stomping and fuss.

Thought: "Well, assholes!!!"
Looked out - and indeed goats...""",
410656: """ххх.: Today bought programmer meat
ххх.: 1024 gr. for 256 rubles""",
410657: """Wife went to an opera concert. Came back delighted. Says:
- Finally figured out the classification of opera voices.
- And?
- Male from high to low - tenor, baritone, bass. Female - soprano, mezzo-soprano and contralto...
- ...Del?
- Idiot turned inside out!""",
410658: """маман: Drunk with friends again?
сына: No mom, not a drop!
маман: You think I don't see your horizon is tilted?""",
410659: """тоша: after I spent 2 weeks in the hospital, I got really disappointed in German cinema(""",
410660: """xxx: on the couch gravity is significantly higher than on the rest of the earth's surface""",
410661: """xxx: Nothing thickens paint like an open can standing in the heat...""",
410662: """xxx: Went into a cafe-bar, asked for the menu, open the MENU and see SETTINGS and a list below, damn, TINCTURES.
xxx: Need to work less.""",
410663: """-BJIADKO-: And who are you, Ukropsnik or Ykponchuk?
YkpOncHuK: Can't you read?""",
410664: """from a local forum:
Romka73
Off-topic: we once found a naked guy in flippers near our tent in the morning, ask - why are you sleeping here, he - got drunk and didn't make it home; and why in flippers, he - it was cold at night, the only clothes I had were flippers :D""",
410665: """Renting an office in a building with one toilet per floor and after lunch employees usually go wash dishes in the sink located in that toilet. So I'm walking to the toilet with a dirty plate in one hand and a fork in the other. A familiar employee comes out of the toilet, having just taken a piss, and asks: "Going for seconds?..."
If he hadn't run so fast - I'd have stabbed him with the fork""",
410666: """111: A logging enterprise needs bulldozer operators, harvester operators for shift work...
222: Machine gunners, rhino drivers and a couple of dreadnoughts.""",
410667: """7th lvl Conjurer:
In the morning the client's representative runs in and starts chirping:
- Oy, we urgently need an estimate, like today by noon, can you make it?
We try to explain that we need the source documentation to know what and how much. In response:
- What source documentation? It's just a tiny bit - just repair a small extension. Dig a little trench with little shovels, knock off a little plaster with little hammers, just a tiny bit... - And such chirping for 10 minutes. And at the end. - Only we need to fit in the budget, ours is small.
Ryzhik silently goes to his desk, types up the estimate, in an hour submits it, the client signs and runs off all happy.
And now the experts are calling and thanking for lifting their mood. The estimate looks something like:
1. Digging a little trench with little shovels. Some meters. Some rubles.
2. Knocking off a little plaster with little hammers. Just a tiny bit. Inexpensive.
3. Painting little walls with paint. A couple of spots. Pennies.
And so on, according to the chirping, for a hundred lines. In the "Total" column: "should fit in the budget.""",
410668: """ххх: So how was the meeting with the new girlfriend candidate? :)
ууу: Yeah, fuck, as usual. This half-baked businesswoman spent an hour and a half pickling my brain about discrimination of women, their rights, role in society. Like over there in the west, they're not considered the weaker sex, equality with men, etc.
ууу: And then in the same cafe threw a tantrum because I suggested splitting the bill
ууу: And now I'm a goat, a faggot and the ultimate boor, incapable of paying for a girl in a cafe
ххх: Stop trolling girls, you monster xD""",
410669: """xxx: a romantic differs from a realist in that he imagines himself a gold digger while washing the cat's litter box.""",
410670: """xxx: Well, the person doesn't have competent writing skills! What now - kill him? To death?

yyy: Yes!
zzz: YES!!
ttt: Necessarily!""",
410671: """went to the cinema with a friend. settled comfortably in the chair.
я: ooo! gorgeous! like home!
подруга: just think about the movie, not like home, ok!?""",
410672: """xxx: Do you have favorite heroine girls from childhood? Mine are all Disney: Cinderella, Snow White, Little Mermaid and Jasmine
yyy: And mine are Sasha Grey, Eva Angelina, Jesse Jane and Rachel Starr""",
410673: """xxx: On the second day I pull myself together and wash the dishes.
yyy: Sounds like the start of a scary rhyme in which on the tenth day everyone dies.""",
410674: """ххх: Today was at the market. At the meat department stood a husband and wife, behind the counter two hefty saleswomen.
ххх: The wife asks the husband "Maybe get something from meat?"
ххх: To which the saleswoman in a brisk voice announced "Choose! There are two of us here\"...""",
410675: """XXX: I got a brilliant idea. Several thousand programmers gather and write an AI capable of performing all functions of officials, for example, counting money, answering questions about laws, etc. When they write this miracle, they'll upload it to the biggest server in Russia, and overthrow the government. Replace all officials with terminals, and everyone will be happy.
YYY: Then someone will need to watch over this thing.
XXX: I thought it all through. Sysadmins will come to power. They'll monitor the system's operability.""",
410676: """XXX: whatcha doing?
YYY: going to take a shower, you?
XXX: did you decide to seduce me? ;)
YYY: No. I decided to wash myself""",
410677: """On a forum.

Таня: Advise a sedative. Only cheap if possible and sold without prescription

Нинель: Here's a good recipe.
Get a job at a farm as a milkmaid where cows are milked by hand. You have a group of cows - 30 head. They're milked 3 times a day. Morning at 5, day at 13 and evening at 20. In a week all the psycho quirks will vanish like magic.""",
410678: """Quq:
In 15 years:
-And we have a Russian language lesson. Kids, who will tell me the root of the word "откололось"?
-Maryvannovna! Can I?
-Yes, Vova?
-I know! The root in the word "откололось" is "ololo"!""",
}

def main():
    src = '/home/hatch/workspace/translation-work/bashim/chunks/chunk_28.jsonl'
    outp = '/home/hatch/workspace/translation-work/bashim/out/chunk_28.jsonl'
    recs = {}
    with open(src, encoding='utf-8') as f:
        for line in f:
            line = line.strip()
            if line:
                r = json.loads(line)
                recs[r['id']] = r['text']
    assert len(recs) == 1000
    for i in EN:
        assert i in recs, i
    cur = 0
    if os.path.exists(outp):
        with open(outp, encoding='utf-8') as f:
            cur = sum(1 for line in f if line.strip())
    with open(outp, 'a', encoding='utf-8') as f:
        n = 0
        for i in sorted(EN):
            f.write(json.dumps({'id': i, 'ru': recs[i], 'en': EN[i]}, ensure_ascii=False) + '\n')
            n += 1
    print('batch28_6 OK, wrote', n, '(output now', cur + n, 'lines)')

if __name__ == '__main__':
    main()
