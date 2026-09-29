# -*- coding: utf-8 -*-
"""Translate chunk 22, records 600-699 (IDs 404669-404768)."""
import json, re

PATH = "/home/hatch/workspace/translation-work/bashim"
SRC = f"{PATH}/chunks/chunk_22.jsonl"
OUT = f"{PATH}/out/chunk_22.jsonl"

src_lines = open(SRC, encoding="utf-8").read().splitlines()
by_id = {json.loads(l)["id"]: json.loads(l) for l in src_lines}

batch = [json.loads(l) for l in src_lines[600:700]]
assert len(batch) == 100
ids = [r["id"] for r in batch]
assert ids == list(range(404669, 404769)), (ids[0], ids[-1])

# Cyrillic <...> nick from 404755, byte-identical from source
nick755 = re.findall(r"<[^>\n]*>", by_id[404755]["text"])[0]

en = {}

en[404669] = (
'111\n'
'A friend has a problem. Computer won\'t boot. Stuck at the Windows boot level\n'
'\n'
'222\n'
'1.reboot in safe mode\n'
'2.check for viruses\n'
'\n'
'111\n'
'That\'s our accounting lecturer\n'
'\n'
'222\n'
'well, if it\'s the accounting lecturer, then\n'
'1.reboot in safe mode\n'
'2.check for viruses'
)
en[404670] = (
'xxx: So what do I do with that ICQ now\n'
'xxx: That I downloaded\n'
'yyy: install it\n'
'xxx: why\n'
'yyy: then don\'t install it\n'
'xxx: why?'
)
en[404671] = (
'Riding the metro, the train stops at the station and stands, stands, stands... Dead silence, can hear the words of a song playing in some guy\'s earphone. Suddenly someone\'s phone rings, a military guy to my right takes out an old Nokia, thoughtfully looks at the screen and starts the conversation:\n'
'-Captain Radimov speaking!\n'
'After a few seconds:\n'
'-F...ck, you\'re Captain Obvious yourself!'
)
en[404672] = (
'xxx: What\'s your Skype nick?\n'
'yyy: nu_hot_etot_nik_svoboden'
)
en[404673] = (
'ххх: I keep dreaming the same dream\n'
'ууу: what?\n'
'ххх: like I overslept for work\n'
'ххх: wake up in a cold sweat\n'
'ххх: everything\'s fine\n'
'ххх: only my arms go numb\n'
'ххх: not comfortable sleeping on the desk'
)
en[404674] = (
'ххх: So, the speakers are 35W, black, and gray on top.\n'
'xxx: Actually no, that\'s dust. Fully black!'
)
en[404675] = (
'xxx: Damn, that\'s what professionalism means!\n'
'xxx: I come out to the kitchen, all warm, sleepy, there\'s Druzhische walking around the kitchen talking on the phone with a client.\n'
'xxx: Laptop on the table, freshly poured coffee, work atmosphere, in short. And then I hallucinate that my phone is ringing, I rush sharply into the room, tripping over the laptop\'s twisted pair on the way.\n'
'xxx: Terrible crash, two broken cups, the WHOLE kitchen flooded with an even layer of sweet coffee, total armageddon.\n'
'xxx: But Druzhische not only didn\'t say "Fuckme!!" or at least "Fuck!!.." into the phone to the client, like I would have, he didn\'t even lose his intonation!'
)
en[404676] = (
'Stinx: yesterday got a roll of toilet paper.\n'
'Stinx: if you look at the tear lines - it goes three short pieces, three long and three short again.\n'
'Stinx: it must be made by former military signalmen, driven into a bunker and working for food!\n'
'------\n'
'hint: ...---... is SOS in Morse code'
)
en[404677] = (
'KOXA: did everyone congratulate you on your birthday?\n'
'Lanka: not everyone yet. well, mom as always woke me at six in the morning\n'
'KOXA: why so early?\n'
'Lanka: it\'s her thing. either to always be the first to congratulate, or so I don\'t get enough sleep. and then I get depressed because of it...\n'
'KOXA: and what time were you born?\n'
'Lanka: at three in the morning\n'
'Lanka: are you saying she\'s getting back at me for that?'
)
en[404678] = (
'King: you\'re really 23?\n'
'Marina: yeah, and you\'re really 12\n'
'King: weell\n'
'Marina: cool\n'
'Marina: I even have a husband\n'
'King: let\'s hide from your husband\n'
'Marina: ) aren\'t you scared?\n'
'King: nope\n'
'Marina: you should be\n'
'Marina: I\'d be scared of a thirty-year-old man in your place\n'
'King: no no\n'
'Marina: so what\'s the plan?\n'
'King: let\'s fuck\n'
'Marina: no, marriage first\n'
'King: to me?\n'
'Marina: to whom else?\n'
'King: ok\n'
'Marina: I do have a kid though...\n'
'King: I\'m a kid too\n'
'Marina: exactly, so no fucking, I can take you to the movies and McDonald\'s\n'
'King: let\'s go!!!!!!!\n'
'Marina: will they let you go with a strange lady?\n'
'King: yes'
)
en[404679] = (
'DDD: A stranger sees a factory as an enterprise.\n'
'DDD: The tax inspection - the biggest taxpayer.\n'
'DDD: The director - scrap metal that can be sold somewhere.\n'
'DDD: And the odmin sees four floors of dumbasses.'
)
en[404680] = (
'xxx: THEY only watch those who are paranoid'
)
en[404681] = (
'Iceman: I think I didn\'t get enough sleep today\n'
'xxx: You say that every morning\n'
'Iceman: yeah, but not every morning do I pour mayo into my coffee'
)
en[404682] = (
'xxx: Why, when I look at an iguana, do I keep feeling it\'s about to say "croak"?\n'
'yyy: Fuck knows, the hairy bastard.'
)
en[404683] = (
'Anya: My mom, when she leaves for a long time, leaves me notes like: "Anya-flowers", "Anya-dishes", "Anya-floor"...\n'
'Anya: But that\'s nothing. The cruelest is - "Anya-cat litter box"\n'
'Anya: I\'m not a cat litter box >_>\''
)
en[404684] = (
'he: what are you doing now?\n'
'she: I\'m grinding coffee\n'
'he: praying about what?'
)
en[404685] = (
'Lucky Man ®: like dicks?\n'
'Leela: of course, I\'m a girl\n'
'Lucky Man ®: and what kind do you like?\n'
'Leela: those with a smart man on one end :)\n'
'Lucky Man ®: how\'s that?\n'
'Leela: forget it'
)
en[404686] = (
'Xxxx: yesterday I heard how they told our operating systems lecturer about the internship?\n'
'\n'
'Yyyy: no\n'
'\n'
'Xxxx: some woman approaches him in the corridor, says, three fourth-years will come to you tomorrow, they have practice, you\'re the supervisor, help them out. He nods, looking around slyly, the woman leaves, he turns to me, rubs his hands and drawls "deaaad meeen"'
)
en[404687] = (
'Vano: what to do?\n'
'VinT: with what?\n'
'Vano: in general\n'
'VinT: why do anything, live and rejoice in life! )\n'
'VinT: around is joy, smiles, love\n'
'VinT: bunnies splashing in puddles, kids playing in the yard, the sun shining, birds singing\n'
'VinT: only all this is not here =\\'
)
en[404688] = (
'fuck-n-dead:\n'
'I\'ll give you a condom, pass it to your parents and tell them not to do that anymore'
)
en[404689] = (
'From a ventilation design forum:\n'
'\n'
'xxx: Hello, esteemed ones.\n'
'Has anyone designed ventilation in a crypt? I haven\'t found anything on them yet....\n'
'\n'
'yyy: If the client often comes to mourn the ancestors\' ashes, let him have 20 m3/h per each of those present simultaneously. If he cries more than 4 hours - 60 m3/h per person and if more than four hours and cries actively with sprinkling heads with ash and various frequent gymnastic equivocations, then 80 m3/h per person.'
)
en[404690] = (
'Lora: we decided to have sex at his work\n'
'the server room temperature is +18\n'
'so I wouldn\'t be cold he raised the temperature\n'
'as a result three servers burned out'
)
en[404691] = (
'muravey: just saw 4 homeless guys in the park, they were eating doshirak\n'
'muravey: came, looked at mine and somehow lost the appetite)\n'
'Beer Baron: well if there\'s no difference, why go to work?'
)
en[404692] = (
'N@@K@: Heard, Serega got in an accident, unknown if he\'ll walk now ((((\n'
'sadken: thanks, I\'ve been informed. I wonder if he\'ll change his nick now.\n'
'RoadRunner: JUST FUCKING HILARIOUS!\n'
'RoadRunner: I won\'t.'
)
en[404693] = (
'Wife on Friday evening:\n'
'- Let\'s swap bodies for the weekend! You\'ll cook food, and I\'ll DESIRE you!'
)
en[404694] = (
'Dima &#8206;(9:16): Hi.\n'
'Alien &#8206;(9:16): hi\n'
'Dima &#8206;(9:27): I\'m all good. Working sort of. Might get sent on a business trip this month. How are you? What\'s new?\n'
'Alien &#8206;(9:27): uhh\n'
'Dima &#8206;(9:29): No shit. How long have you had that?\n'
'Alien &#8206;(9:29): weell\n'
'Dima &#8206;(9:29): No, can\'t today. Busy after work.\n'
'Alien &#8206;(9:29): mm\n'
'Dima &#8206;(9:29): Maybe tomorrow.?\n'
'Alien &#8206;(9:29): ah\n'
'Dima &#8206;(9:30): Right, let\'s do tomorrow. What time?\n'
'Alien &#8206;(9:30): well\n'
'Dima &#8206;(9:30): Let\'s do a bit later.\n'
'Alien &#8206;(9:30): yup\n'
'Dima &#8206;(9:31): Good then. Agreed. Can\'t talk about everything in ICQ anyway. Can\'t tell everything.\n'
'Alien &#8206;(9:37): yeah\n'
'Dima &#8206;(9:39): How long ago did this happen to you?\n'
'Alien &#8206;(9:39): so...\n'
'Dima &#8206;(9:40): Are the spots red or blue?\n'
'Alien &#8206;(9:41): wha'
)
en[404695] = (
'yyy: hiiii!!!! whatcha doing??? what\'s new???\n'
'xxx: Hey Sanya, studying, working, sometimes eating. what\'s new... new pants and haircut ) what have you got to brag? )\n'
'yyy: and I\'m serving in the army right now!!((((\n'
'xxx: so same shit, yeah...'
)
en[404696] = (
'xxx: and don\'t tell me about the neighbor\'s drill on Saturday morning. I live above the registry office... ('
)
en[404697] = (
'xxx: In Simferopol there\'s also a pub "Proletarsky" :) a beer restaurant in pseudo-Soviet style. They brew beer themselves.\n'
'xxx: Light "Future", red "Present" and dark "Past".'
)
en[404698] = (
'xxx: The radio announcer said it cool today\n'
'xxx: Now in Ivanovo and its surroundings, including Moscow, 7 hours 10 minutes..'
)
en[404699] = (
'xxx: Today I remembered for life that you need to check the oven before turning it on.\n'
'xxx: Kids can put a cat in there.'
)
en[404700] = (
'Corporate mailing:\n'
'Park IT (our IT guys):\n'
'Colleagues, hello! This is Alexey! I\'m informing you that from ** to ** I\'ll be on vacation. For all questions contact Alexander.\n'
'\n'
'Park IT (2 minutes later):\n'
'Colleagues please no. Better wait until Alexey comes back from vacation.'
)
en[404701] = (
'жжж: when I lived alone and worked from home, for a while I was friends with the paperclip in Word.'
)
en[404702] = (
'From a forum:\n'
'A friend of mine got a call at home from her husband\'s mistress saying "do you know your husband is cheating" to which the friend didn\'t lose her cool and answered "let him walk, he\'s dressed warmly". The mistress got angry and hung up )'
)
en[404703] = (
'*S*A*M*: where\'d the heating go?\n'
'Stratus: That was probably a test switch-on\n'
'*S*A*M*: Liked it? Send an SMS to the number...'
)
en[404704] = (
'xxx: get my September salary and buy a new laptop. Acer\n'
'yyy: I think better to wait, save a bit and buy a Mac\n'
'xxx: instead of a Mac better to wait a bit more and buy a Lada Kalina))\n'
'yyy: in that case better to wait a couple years and buy a Bentley\n'
'xxx: then better to wait more and buy a sapphire coffin)'
)
en[404705] = (
'<xxx> yesterday scared thieves at 3 AM. wanted to take the garage, jingling, jingling at the lock...\n'
'<xxx> their professionalism pissed me off. took a powerful flashlight, climbed out the window, turned it on, pointed at the lock and asked "need some light?"\n'
'<xxx> they ran fast )'
)
en[404706] = (
'Mom: So is your Dimka getting ready for the baby?\n'
'Coworker: Yep. Swaddling the cat.'
)
en[404707] = (
'xxx: yesterday felt ashamed - came back from the dacha, the kid playing cars in his room, I hear "where you going asshole", "what you standing for, let\'s go", "where you driving, moron".\n'
'xxx: In short, I need to shut up behind the wheel'
)
en[404708] = (
'Lisa: Today almost quarreled with my boyfriend\'s mother...(\n'
'Anna: ???\n'
'Lisa: We call each other pet names - he calls me: Kitty, I call him - Mousey.\n'
'Lisa: going to his place and see a flock of mice near his entrance.\n'
'Lisa: enter his apartment and from the doorway, without thinking, jokingly told him: all your relatives are rummaging in the trash box near the garbage chute...\n'
'Lisa: I didn\'t notice right away his mother was standing nearby...'
)
en[404709] = (
'xxx: By the way, most viruses are brought on flash drives of certain individuals >:o\n'
'yyy: according to the 20/80 rule?\n'
'yyy: 20% of individuals carry 80% of viruses, 20% of people drink 80% of beer, etc.\n'
'yyy: isn\'t that the "golden billion" theory?\n'
'xxx: that\'s already smelling of masons :)\n'
'yyy: that smells of Gaussian distribution.'
)
en[404710] = (
'1: A year ago paid the fee for getting my license, a young employee at our Sberbank took it, knew nothing, was afraid of everything.\n'
'A YEAR PASSED, NOTHING CHANGED!\n'
'2: sympathize\n'
'1: It\'s funny to you. I was third, standing 40 minutes already\n'
'1: Damn! She\'s typing with one finger\n'
'1: "Sberbank"... Keeping traditions since 1841...'
)
en[404711] = (
'xxx: damn, rain again. hate rain in autumn. =((((\n'
'yyy: and I don\'t like it in summer. immediately dreary and like autumn.\n'
'zzz: and I\'ve really disliked rain in winter in recent years...'
)
en[404712] = (
'xxx: downloading a bad torrent is like digging a tunnel out of prison\n'
'xxx: only at night and two meters a day...'
)
en[404713] = (
'xxx: I\'ve happened before to find various ladies\' accessories in public places, like: bras, panties, tights... Everyday stuff.\n'
'xxx: And here I walk into the men\'s room, and on the windowsill lie beads. Ordinary women\'s beads. Light blue ones.\n'
'xxx: First thought: "Why the hell did he take the beads off her?!"'
)
en[404714] = (
'xxx: Listen, downloaded such a cool game - Risen. Played a bit - really reminds of Gothic but much better performance-wise. Already played 15 hours, don\'t you want to play too?\n'
'yyy: Yeah, would be nice. And haven\'t you heard maybe they\'re making part 2? Like, pilaf or macaroni?'
)
en[404715] = (
'bbl321_: and I have grapes\n'
'* bbl321_ showed everyone a black tongue\n'
'Dill: and I have Gentoo\n'
'* Dill showed everyone red eyes'
)
en[404716] = (
'OleSha: Congrats on the twins!!!!\n'
'Roy: You\'d better pity me :)\n'
'OleSha: Come on, kids are the flowers of life!!!\n'
'Roy: Yeah, two carnations)'
)
en[404717] = (
'girl: Where are we going today? :)\n'
'boy: Weeell, we have an extensive cultural program ;)\n'
'girl: I\'m NOT going to your dorm!!!'
)
en[404718] = (
'XXX: Did you upload the archive to rapidshare?\n'
'YYY: Considering it weighs 51Kb, more like "dripped", not "uploaded"'
)
en[404719] = (
'sssr1945: Going to play tennis tomorrow?\n'
'Yukatan: Yep. And Misha wants too. But too bad there\'s only three of us, need a fourth. No more takers?\n'
'sssr1945: Nope...\n'
'sssr1945: Well let\'s the three of us chip in for a prostitute for 2 hours, and let her play tennis with us!'
)
en[404720] = (
'arbalet: I don\'t even have a cat... that\'s the irony of fate\n'
'arbalet: mice peed in my boots ('
)
en[404721] = (
'xxx: The vet wrote in the medical card of my friend\'s dog named Yodo: "Wound healing is observed."\n'
'xxx: Expert, damn..'
)
en[404722] = (
'xxx: today went to the store to buy new boots\n'
'ххх: walk in, start looking at goods. The place is huge - rows of shoes packed.\n'
'ххх: go further and notice someone coming straight at me from the other end of the store. About to collide. And I\'m nearsighted -4, see badly.\n'
'ххх: and what do you think? I stopped - he stopped. and only after 2 seconds it hit me that it\'s a huge mirror. no wonder I flinched when I saw myself'
)
en[404723] = (
'Werkzeug: Only at our place! Make your cat happy! Legally! Audio valerian!'
)
en[404724] = (
'Nataly\n'
'kids\' humanism is really a non-trivial thing. on the weekend bought crayfish. Antokha was really worried they\'re bad in the basin, cold etc., but with no thought at all threw them into the pot'
)
en[404725] = (
'She: kitty, are you coming today?\n'
'He: sweetie, let\'s not today\n'
'She: come ooon! Always like that. Always stingy. You even begrudge me the gas.\n'
'He: my sunshine, I don\'t begrudge you gas, drink up!'
)
en[404726] = (
'Max has several favorite cartoons, in particular "Treasure Island" and "Three from Prostokvashino". Sometimes it comes out like this:\n'
'\n'
'- Who\'s there? Postman Pechkin, brought a black spot for your boy!'
)
en[404727] = (
'Ilya: why write new songs?! we don\'t need new songs yet! we have them!\n'
'Sasha: damn! we have only 3 songs!\n'
'Ilya: and what\'s the third?'
)
en[404728] = (
'From a philosophy forum:\n'
'\n'
'Quote:\n'
'> Has anyone been a hair\'s breadth from death? If yes, recall that state.\n'
'> That\'s how you should live.\n'
'\n'
'Is that standing on cotton, buckling legs, and with pissed pants?'
)
en[404729] = (
'(A computer science teacher about students who play games in class)\n'
'\n'
'xxx: I told them one interesting thing\n'
'xxx: They\'ll play only on one condition\n'
'xxx: If they write the game themselves\n'
'xxx: You won\'t believe: I immediately met the authors of Counter-Strike and Warcraft'
)
en[404730] = (
'xxx: That turns me on )\n'
'yyy: What?\n'
'xxx: You and programming )\n'
'yyy: Hm. First time I hear programming turns someone on.\n'
'yyy: Though there\'s plenty of sex with it, especially at the testing stage )\n'
'xxx: Especially when you tell about it. Rrrr.\n'
'yyy: Touch my python, and I\'ll caress your c++ )'
)
en[404731] = (
'ооо: and I think girls are great sinners. if hypothetically believe the tale about god, the author writes - not verbatim, conveying the meaning - Eve crossed the line of the law and sinned, tasting the forbidden fruit. only let\'s not talk about the serpent being a man and it being all his fault.'
)
en[404732] = (
'Lama> All my life I was afraid the upstairs neighbors would flood me\n'
'Lama> ...until the downstairs neighbors burned down'
)
en[404733] = (
'<hatarisu> Studying Windows 7 features by phone!\n'
'<hatarisu> Consulting a client on security settings of a system I\'ve never seen with my own eyes!\n'
'<hatarisu> Hope he hasn\'t figured it out yet...'
)
en[404734] = (
'xxx: Revenge is terrible. And sometimes deadly. My cat knew two ways of terrible revenge - to gnaw through and to pee.\n'
'ххх: Last night he decided to finish off my comp. First he gnawed the plug and the power strip.\n'
'ххх: Then sat down to pee.\n'
'ххх: The stream hit the power strip, everything shorted.\n'
'ххх: Pity, of course. Today going to buy a new power strip. And a new cat.'
)
en[404735] = (
'[Lord_ArronaX] "Dancing with the Stars", so that\'s where all this heresy came from! Though, like practically all other popular shows on domestic TV.\n'
'[carrot] That\'s not heresy, that\'s the right show! After it citizens flooded into dance clubs!\n'
'[Griffon] Bring on shows: "Morning exercises with the stars", "We don\'t drink beer with the stars" and "We don\'t shit in the entrance with the stars"!'
)
en[404736] = (
'Ark: buddy serves in the army\n'
'Ark: 10 months not a single word\n'
'Ark: Yesterday came an SMS - Mikhalych, it\'s Lyokha, check the bot in Giran and give 300kk to Diastrez, he\'s our company commander :('
)
en[404737] = (
'Sanya - a naturally huge guy, in build and appearance like Valuev.\n'
'WoW guild chat\n'
'\n'
'xxx: And why isn\'t Sanya here?\n'
'yyy: Gopniks harassed him yesterday...\n'
'xxx: And what, he\'s in the hospital now?\n'
'yyy: ROFL, in the police station, the cops didn\'t believe him when he said they attacked him, not he them =)'
)
en[404738] = (
'ххх: At the "Mathematics" class our group derived the following formula from the lecturer\'s fragmentary words: The limit of a group of students, as the number of students in class tends to zero, equals the class monitor.'
)
en[404739] = (
'Natali: thanks!\n'
'Natali: you\'re the best! the smartest )\n'
'Me: you owe me a banana\n'
'Natali: but mercenary'
)
en[404740] = (
'Almagesta: yeah, I don\'t have doll beauty, but nobody needs it. But I have long legs and a brain\n'
'WildRain: and how long is the brain? :)'
)
en[404741] = (
'ZCm: The difference between proliferated universities and buying a diploma in the metro is that in the first case you get installments.'
)
en[404742] = (
'xxx: Came to my wife\'s work, sat at the secretary\'s comp, launched "Opera", closed in "Opera" 52 tabs with an offer to verify Windows authenticity, 11 tabs with the start page and two tabs with porn sites.'
)
en[404743] = (
'ххх: When we were at army training camp, we lived in tents. In big ones, army ones. There were wooden pallets instead of floor. So when we were leaving, we took those tents apart and carried the pallets away. And under the pallets there was a pile of small change. You should\'ve seen how they collected that change. So not a single coin stayed on the ground.\n'
'yyy: :D nobody wanted to come back )'
)
en[404744] = (
'katya: don\'t you remember which song from Monowar we liked?\n'
'yulya: Stereowar?'
)
en[404745] = (
'From LJ:\n'
'Well damn.\n'
'My conscience just woke up: forgot my pass, rode as a hare for two days, and now I think - I\'ll pay.\n'
'Nope: "Oh, miss, I ran out of tickets, ride like that".\n'
'They just don\'t leave me a chance.'
)
en[404746] = (
'lola: my eyes are my mom\'s\n'
'diamant: and your tits are your dad\'s?\n'
'lola: fuck you\n'
'diamant: no, but really?\n'
'lola: ARE YOU AN IDIOT?!! MY DAD HAS NO TITS!!!\n'
'diamant: well that\'s what I\'m saying :)\n'
'lola has quit IRC. (Quit)'
)
en[404747] = (
'A dorm friend told recently.\n'
'ххх: Just now with Irka talked through the outlet, and she goes: "Are you eating chocolate?"\n'
'ххх: I\'m like stunned: "Yeah. So?"\n'
'ххх: She: "It just smells through the outlet".\n'
'ххх: Yeah, hard days at the dorm these days'
)
en[404748] = (
'xxx: can you lip-read?\n'
'yyy: a little\n'
'yyy: "what\'s your fare?" "I see" "ok" "got a smoke?" "take out the earphones"'
)
en[404749] = (
'tasha: just now a girl came in and told a funny story.\n'
'tasha: she took her daughter\'s skirt to the dry cleaner. the skirt had only two buttons, the third tore off and was at home. at intake they pointed this fact out to her. she answered it\'s fine, it\'s at home. picks up the skirt today, and it has THREE buttons. on closer inspection they turned out to be big military buttons with MVD inscription\n'
'tasha: the kid refuses to wear THAT to school\n'
'tasha: she went to the dry cleaner and asks: what\'s this gesture of goodwill? they tell her: you see, under strong steam treatment your buttons melted. and we sewed on the buttons we had available. she tells them: but we have a set! both the jacket and the vest, and they to her: well here you go! and dump a bag of buttons on the counter! all MVD, military unit, KGB.'
)
en[404750] = (
'ххх: Answer me one question.. Are you stupid?\n'
'ууу: No!\n'
'ххх: Wrong answer.'
)
en[404751] = (
'xxx: I wonder, which street in our city is the longest?\n'
'yyy: Kirova of course :)\n'
'xxx: Damn, how do you know that?\n'
'yyy: What do you mean, I worked at a real estate agency!\n'
'xxx: :o And after Kirova?\n'
'yyy: Petrovskogo\n'
'xxx: Hm... I wonder, why would a real estate agency need to know which streets are the longest?\n'
'yyy: ... Well... Had lots of free time...'
)
en[404752] = (
'Fox: I work at a company called "Ilma"\n'
'Fox: today they announced a contest for the best name for the corporate newspaper\n'
'Fox: so far the leading variant by a large margin is "Ilmanakh"'
)
en[404753] = (
'Vitaminka: I got sick\n'
'Space84: take your nicks\n'
'Vitaminka: Whom?\n'
'Space84: nicks\n'
'Vitaminka: What is it?\n'
'Space84: let\'s go from the other side, how did you come up with your nick?'
)
en[404754] = (
'coo9snic: You know, I\'m tired of getting together-breaking up with her.\n'
'coo9snic: What am I, a limit or a series? :(('
)
en[404755] = (
nick755 + ' it\'s gotten quiet somehow...\n'
+ nick755 + ' maybe unban?\n'
'<molatok> that\'s how amnesties happen in our country.'
)
en[404756] = (
'garmonika0707: Next week four new employees are coming, and there\'s no space in the office. Right now Maximko sits in the general\'s office, and the lawyer and department head come to the office in shifts. And we have a new principle - lift your ass, lose the wheeled chair )\n'
'Djokonda: That\'s sacred!!!\n'
'garmonika0707: it\'s a shortage ) now if the owner of a wheeled chair goes on business to another office, he drags the chair with him )\n'
'Djokonda: Irrational!!! Why not ride the chair?\n'
'garmonika0707: because then there\'ll be traffic jams in the corridor'
)
en[404757] = (
'xx: at ten I played Indian — rigged a solid bow from hazel, and how I planted a well-sharpened arrow-lath right between grandpa\'s shoulder blades.\n'
'yy: didn\'t grandpa tear your arms and legs off?\n'
'xx: that day I learned that old grandpa can quickly run four kilometers without stopping, while lashing my ass with a willow switch.'
)
en[404758] = (
'*****: Light of mine, mirror, tell, report the whole truth: who in the world is the sweetest, the rosiest and whitest?!\n'
'Dingo-Chg: I don\'t know shit, but it\'s probably ME!'
)
en[404759] = (
'ххх: Imagine: I brewed you tasty black tea; brought it to you with cookies. The tastiest, crumbliest sesame cookies. You already feel the tea aroma, anticipate the cookie taste... AND I TAKE AND THROW THE COOKIE INTO THE TEA!!!\n'
'yyy: ??\n'
'xxx: Did YOU feed the cats?\n'
'yyy: Yes, I...\n'
'xxx: THEN WHY THE FUCK DID YOU POUR WHISKAS INTO THEIR WATER?!'
)
en[404760] = (
'Reading aloud our district news, Fedya sitting nearby.\n'
'Me: A few houses from us a skeleton was found in a garbage container. According to experts the body had been lying there for about a year.\n'
'Fedya: *melancholically, without looking away from the monitor* They rarely take out the garbage in our yards'
)
en[404761] = (
'yyy: hi, truant\n'
'xxx: I was at an operation, they cut off my ears.\n'
'yyy: for what?\n'
'xxx: So I wouldn\'t scratch, otherwise there\'d be serious problems with the ear canals.\n'
'yyy: hm... why are they itchy?\n'
'xxx: Who knows why he keeps scratching them.\n'
'yyy: oh, you\'re talking about the dog xD\n'
'xxx: Who did you think I meant?'
)
en[404762] = (
'<xxx> one of my classmates told about a student from Latin America with a double name and double surname.\n'
'<xxx> they enrolled him in different groups\n'
'<xxx> first name in one group, surname in another'
)
en[404763] = (
'Ellie:\n'
'About the harsh humor of defense workers )\n'
'Mishka told me today.\n'
'He works at a defense enterprise, remember?\n'
'So. Today he\'s having lunch in the canteen with the head of the neighboring department. Sitting, peacefully chatting, bothering no one. In the middle of the conversation an HR employee runs up, whose daughter studies at the institute and works half-time at their institution. And she jumps on the boss:\n'
'- Alexander Timofeevich! Why did you scare my child?! She has a term paper "10-year enterprise development plan".\n'
'- Why did you tell her that in three years World War III will start?!'
)
en[404764] = (
'xxx: son bought a little jeep the day before yesterday, Isuzu VehiCROSS\n'
'ууу: what kind of beast is that?\n'
'ххх: here\'s a link to ***.ru, they\'re selling the same one there, only wrecked.\n'
'ххх: oh, and the guy\'s named like my rascal\n'
'ххх: fuck, and his phone\'s listed. I\'ll kill the bastard!'
)
en[404765] = (
'fat38: By the way as a kid I was sure a hole puncher was for making confetti..'
)
en[404766] = (
'xxx: And still service has gotten better. I remember when we were in school, you come to the pharmacy, ask for condoms, and she with a sly squint and across the whole pharmacy: "Isn\'t it early yet!?" And now you come, ask for condoms, and she politely: "Which ones would you like?"\n'
'yyy: No, Sanya, service has nothing to do with it. You just grew up.'
)
en[404767] = (
'(daughter is 2 years 4 months)\n'
'xxx: I thought, any day now the period of "how, why, what for" questions would start - "why does the sun shine", "why is grass green and the sky blue", "how do birds fly" etc. All kids pester with such questions by age 3. Maybe buy a kids\' book - with such questions and answers, bright, with pictures. Wrong guess.\n'
'\n'
'xxx: Today was the first such question... "how does a laptop work?". 5 minutes later the second - "how does a phone work?". 5 more minutes later - "why does this phone work (my mobile), and this one doesn\'t (the toy one)?"\n'
'\n'
'xxx: thinking of getting a physics textbook instead of that kids\' encyclopedia...'
)
en[404768] = (
'xxx: and I also have a hamster\n'
'xxx: little\n'
'xxx: vaska\n'
'xxx: he\'s a girl, actually, but I think he doesn\'t care'
)

assert set(en.keys()) == set(ids), (set(ids) - set(en.keys()))

# Newline parity check before writing anything
bad = []
for r in batch:
    ru = r["text"]
    e = en[r["id"]]
    if ru.count("\n") != e.count("\n"):
        bad.append((r["id"], ru.count("\n"), e.count("\n")))
if bad:
    raise SystemExit(f"NEWLINE MISMATCH: {bad}")

# <...> token preservation check
bad = []
for r in batch:
    toks = re.findall(r"<[^>\n]*>", r["text"])
    for t in set(toks):
        if t not in en[r["id"]]:
            bad.append((r["id"], t))
if bad:
    raise SystemExit(f"NICK MISMATCH: {bad}")

with open(OUT, "a", encoding="utf-8") as f:
    for r in batch:
        rec = {"id": r["id"], "ru": r["text"], "en": en[r["id"]]}
        f.write(json.dumps(rec, ensure_ascii=False) + "\n")

n = sum(1 for _ in open(OUT, encoding="utf-8"))
print(f"appended 100, out now has {n} lines")
