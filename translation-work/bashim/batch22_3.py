# -*- coding: utf-8 -*-
"""Translate chunk 22, records 300-399 (IDs 404369-404468)."""
import json

PATH = "/home/hatch/workspace/translation-work/bashim"
SRC = f"{PATH}/chunks/chunk_22.jsonl"
OUT = f"{PATH}/out/chunk_22.jsonl"

src_lines = open(SRC, encoding="utf-8").read().splitlines()
src = [json.loads(l) for l in src_lines]

batch = src[300:400]
assert len(batch) == 100, len(batch)

ids = [r["id"] for r in batch]
assert ids == list(range(404369, 404469)), (ids[0], ids[-1])

en = {}

en[404369] = (
'From a girl\'s resume: "...I possess knowledge of PC..."'
)
en[404370] = (
'A driver from work told me:\n'
'Traffic jam on the highway, the flow barely moving. Down the emergency lane flies a DPS Ford with its lights on. Behind it, of course, the clever ones latched on as a tail.\n'
'\n'
'And suddenly the Ford slams the brakes... everyone brakes... drives on again - everyone drives... slams the brakes sharply again, and so on repeatedly. Then from the loudspeaker: "Come on kids, quickly scatter into your lanes. Or I\'ll put you in the corner."'
)
en[404371] = (
'One friend told me:\n'
'Tula, Airborne Troops Day, walks a pair: a drunk yelling paratrooper and a girl trying to calm him down.. They pass a fountain, the paratrooper turns to it, yells "My bunny!!" and runs into the fountain. The girl hysterically yells after him: "I\'m your bunny, idiot!!"'
)
en[404372] = (
'about English pop:\n'
'- Elton John - the Sirius of English pop!\n'
'- Didn\'t get it 8(\n'
'- I mean - a star...\n'
'- A-a-a. A blue giant?\n'
'- YES!'
)
en[404373] = (
'xxx\n'
'Saw an ad on the wall "Growing nails, arms and legs 89*******" called, asked them to grow me a couple more arms. Didn\'t get it, suggested they remove the comma from the ad.\n'
'\n'
'xxx\n'
'They called back in five minutes and through loud laughter said "thank you"'
)
en[404374] = (
'Ad: Buying a Planeta 5 in good condition!\n'
'don\'t offer cosmic prices!!!\n'
'\n'
'I didn\'t get right away that he meant a motorcycle )'
)
en[404375] = (
'talk before 5th year:\n'
'xxx\n'
'I was thinking, if in 11th grade I carried a first-grader on my shoulders, then this September 1st they owe me a freshman girl )'
)
en[404376] = (
'xxx: There\'s an ashtray on the balcony.\n'
'xxx: As it fills up the contents get thrown out the window.\n'
'xxx: Can anyone explain why it\'s standing there?'
)
en[404377] = (
'He: What phone do you have?\n'
'She: A beautiful one\n'
'He: The model? :-)\n'
'She: No, why?'
)
en[404378] = (
'Admin: and I also love my job because I have a folding cot in the server room =)'
)
en[404379] = (
'CEO talking to the HR chief:\n'
'CEO: Fyodor, I came up with a new motivation system for our company:\n'
'We\'ll let employees steal a little, and when we need to pressure them I\'ll report that I knew about the schemes all along and it could end badly for them.\n'
'HR: Yes, but you could lose the team\'s authority and respect, and it could lead to negative consequences.\n'
'CEO: Then YOU\'LL do the blackmailing, Fyodor.\n'
'HR: What? No, I can\'t, I\'m the HR chief. People see me as a pillar and support, no, I can\'t.\n'
'CEO: Yeah, and which one of us drags home a pack of paper a month, and 1C on a flash drive?!'
)
en[404380] = (
'- Why do you love your cat? He\'s an egoist, an ungrateful pig, loves only those who feed him, a rare stinker, in short - a scoundrel.\n'
'- Because he\'s shit - and doesn\'t hide it.'
)
en[404381] = (
'Brick: "They\'re always rushing to work - I\'ve seen thousands of them, with breakfast in their pocket, they run like crazy, thinking only about how to catch the train, in fear they\'ll be fired if they\'re late. They work without getting into it; then rush home, afraid to be late for dinner; in the evening they sit at home, afraid to walk the dark streets; they sleep with wives they married not for love but because they had some money and hoped to secure their miserable existence. Their lives are insured against accidents. And on Sundays they\'re afraid to ruin their soul. As if hell was made for rabbits!"\n'
'- H.G. Wells, "The War of the Worlds", 1897\n'
'­\n'
'vlom: nothing changes )'
)
en[404382] = (
'positive: we were standing in a tight queue and her ass was touching my hand... )'
)
en[404383] = (
'xxx: walking home from your place yesterday, near "Kupets" there\'s a broken bottle of expensive wine, a couple steps further I see a trampled pack of condoms )\n'
'xxx: the evening didn\'t work out'
)
en[404384] = (
'ZoozooZ: Crisis, cracking open the stash, the money\'s in place and with it a note from my little brother: "The feeling of conscience, or rather fear, defeated greed in me"'
)
en[404385] = (
'xxx: found myself a caring girl\n'
'xxx: comes over, cooks, feeds me, then logs into WoW and feeds her pet )'
)
en[404386] = (
'xxx: Is it normal that Homecoming lags at the very beginning?\n'
'yyy: yep.\n'
'yyy: by the way, when you come back with the second half of the x-ray, a corpse will fall on you from the ceiling\n'
'yyy: and at first, when the vid card appears and the caption below "press any key" don\'t press anything for a while. You\'ll see a beautiful video )\n'
'xxx: Um, is this about me going for an x-ray tomorrow?\n'
'yyy: no-no, it\'s in the game\n'
'xxx: You... uh... warn me'
)
en[404387] = (
'Talea\n'
'went to work today, gave mine the task - teach the kid to count to twenty. the kid\'s five.\n'
'\n'
'Talea\n'
'I come back, she\'s counting. to twenty-five. then the caliper runs out. he was teaching her to count on the caliper, the engineer, fuck'
)
en[404388] = (
'Vlal: I\'m so sick of everything. Just drop everything and join the army!\n'
'radiomanka: And I\'ll wait for you!\n'
'Vlal: That\'s what I\'m most afraid of...'
)
en[404389] = (
'Talisman_vmk: (?) Attention question:\n'
'An addition, a correction, that which changes.\n'
'XXX: patch\n'
'YYY: update\n'
'ZZZ: patch\n'
'Talisman_vmk: Correct answer: amendment\n'
'XXX: VMK folks'
)
en[404390] = (
'Annaelle: I think I\'ve overworked\n'
'Annaelle: I have a fridge with a top separate freezer, and a microwave on top of it\n'
'Annaelle: yesterday I come home from work, thinking, now I\'ll heat up food. I put food on the plate - open the freezer - put the food in the freezer - close the freezer - turn on the microwave - leave - the microwave dinged, like all done, - I came, calmly took the food out of the freezer and went to the room - nicely heated up my meal'
)
en[404391] = (
'xxx: "Tula: Communist deputy gave kids from low-income families first-grader kits"\n'
'\n'
'yyy: "a first-grader set" - that\'s 5. I wonder how many first-graders are in a set'
)
en[404392] = (
'shur: he installed a "fishing" game at work\n'
'shur: some Finnish unhurried game\n'
'shur: you pick water temperature, tackle, fishing spot\n'
'shur: pops sits there, catching fish\n'
'shur: in the comp, that is\n'
'shur: so it starts biting, the comp beeped\n'
'shur: so pops did a hookset with the mouse )))))))\n'
'shur: ripped the mouse out by the roots'
)
en[404393] = (
'xxx: it\'s hard to open an archive named pandora.rar'
)
en[404394] = (
'xxx: Cleaning the kettle, then my guy walks into the kitchen, so tenderly says: "well, honey, all steamed up?"\n'
'xxx: To his damn noodles\n'
'xxx: Doshirak'
)
en[404395] = (
'Girl-teamlead persuading the lead programmer to work the weekend:\n'
'- Come on, we need to! And anyway, there\'s no "I" in "team"!\n'
'\n'
'Programmer, thoughtfully looking out the window:\n'
'- But there\'s "meat"...'
)
en[404396] = (
'Cepreu:\n'
'On Sunday I\'ll be very busy: on Wednesday my wife and kids are coming, and I haven\'t washed the dishes'
)
en[404397] = (
'vip02: right now 22 kbytes are loading\n'
'********: ew\n'
'vip02: oh, it\'s 50 now\n'
'********: *ROFL*\n'
'vip02: say "ew" again\n'
'vip02: I need it to reach a hundred'
)
en[404398] = (
'Kuzya: Must be nice watching a movie with beer and your beloved wife...\n'
'Lisa: I\'m not your wife yet )\n'
'Kuzya: But you do the duties =)\n'
'Lisa: Not quite ) I don\'t wash your socks!\n'
'Kuzya: I can wash them myself...\n'
'Lisa: And I don\'t cook!\n'
'Kuzya: I can do that myself...\n'
'Lisa: And I don\'t clean the house!\n'
'Kuzya: I manage fine myself...\n'
'Lisa: Damn, I\'m so glad it\'s me having sex with you, and not you yourself...'
)
en[404399] = (
'xxx: After two past holidays the kiosk near the house reads: "For VDV", and a bit to the right: "For PHP".\n'
'xxx: And all of it with a spray can of the same color.\n'
'xxx: Apparently they drank together.'
)
en[404400] = (
'<MedVeD>: LMAO\n'
'<shaker>: What happened?\n'
'<MedVeD>: Today I lost my phone in the park, turns out my friend picked it up almost right away. He called the last dialed number (my friend) and told him to somehow let me know about it. Where do you think this moron called?\n'
'<shaker>: ???\n'
'<MedVeD>: to the lost phone. who am I friends with?!?'
)
en[404401] = (
'tf8:\n'
'know the three laws of robotics?\n'
'\n'
'LemminG:\n'
'"don\'t break it, it\'ll kill you", "don\'t touch it, it\'ll kill you", "turn it off or it\'ll kill you"?'
)
en[404402] = (
'P$IX> Playing?\n'
'Crist> No\n'
'P$IX> Giving your brain a rest :)\n'
'Crist> My ass.\n'
'Crist> Sat it numb.'
)
en[404403] = (
'Max> Say, where did Egor crash like that?\n'
'WolanD> :)\n'
'WolanD> Ever had it when you come home at night. All tired. Go to your room and without turning on the light flop onto the bed?\n'
'Max> Let\'s say\n'
'WolanD> And can your bed fold up into the wall?\n'
'Max> No\n'
'WolanD> But Egor\'s can..... =)'
)
en[404404] = (
'1: I\'m married to Winnie the Pooh.\n'
'2: ?!\n'
'1: I come home, the wife\'s sitting there. Eating a honey gingerbread, dipping it in honey, and washing it down with honey tea.'
)
en[404405] = (
'nis: outside someone\'s sawing trees near the kindergarten.\n'
'nis: the soundtrack is downright creepy: children\'s screams over the roar of a chainsaw. )'
)
en[404406] = (
'On a volgafishing forum.\n'
'xxx: And suddenly an absolutely amazing sight began. Almost simultaneously, like dolphins, a bream and small breams up to a kilo started jumping, indescribable beauty! After sitting 10 minutes and unable to stand it, we left. So the question is - WHY does he do that???\n'
'yyy: They\'d just never seen a person who wrote fifteen hundred posts in four months...'
)
en[404407] = (
'Work as a hairdresser. A guy with very thick hair sits in the chair. Cutting. I say, like, you have good hair. He says:\n'
'(П)- Oh please! You should\'ve seen it before - clippers broke!\n'
'Naturally asked where the hair went?\n'
'After a while I was informed:\n'
'(П)- Apparently the head grew, but the hair didn\'t increase. So, they spread out...'
)
en[404408] = (
'Smokin\' Ace:\n'
'Riding the metro, listening to music. in front of me stands a charming couple: a beautiful girl, a cute boy. Talking about something, smiling at each other, laughing, unobtrusively kissing. In general - lovely. Decided to take off my headphones, listen to what such cuties might be cooing about. The text approx. as follows:\n'
'He: well fuck I basically told them all to fuck off, like damn you fucking lost it with this project!\n'
'She: like fuck, I\'m fucking in shock! ahahaha!\n'
'...put the headphones back on. decided not to spoil the picture of harmony with the sound of harsh reality.'
)
en[404409] = (
'Gusar:\n'
'Today the elder started 10th grade, the younger 6th. Asked both yesterday - want to go to school? Both answered - no.\n'
'I\'m calm - I\'m raising normal kids.'
)
en[404410] = (
'xxx: Watched the Chinese movie "Battle of Red Cliff".\n'
'xxx: I think it\'s much cheaper for the Chinese to shoot such crowd scenes than to draw them on a computer...'
)
en[404411] = (
'AndreaDoria: you don\'t love me, I\'m leaving you\n'
'\n'
'Art Damage for [AndreaDoria]: Tolyan, don\'t be a wimp, pull yourself together! most of our clan are guys and if we loved you, bad rumors would spread about our clan )'
)
en[404412] = (
'xxx: the movie being low-budget is only half the trouble... there the soldiers\' guns eject whole cartridges instead of spent casings! nice! shot your load, collected, reloaded and back into battle!'
)
en[404413] = (
'from a forum:\n'
'\n'
'He:\n'
'I\'ll love the woman who cooks me borscht.\n'
'\n'
'She:\n'
'X) that\'s a bit too easy)\n'
'maybe specify? borscht that you\'ll like\n'
'\n'
'He:\n'
'Specifying: I\'ll look warily at a woman who cooks borscht that even I won\'t like.'
)
en[404414] = (
'xxx\n'
'I think it\'s time for me to lose weight...\n'
'Sitting alone in the department.\n'
'A woman from the next department peeks in and says: "Goodbye, guys".'
)
en[404415] = (
'xxx:\n'
'Dear applicant.\n'
'Since your resume arrived at our company in unreadable form and contains many question marks, we believe this indicates your uncertainty about your work experience, on the basis of which our company denies you the lawyer position.\n'
'--------------------------------------\n'
'Best regards,\n'
'HR manager\n'
'\n'
'xxx:\n'
'This is a masterpiece of HR stupidity. In reply I sent a suggestion to change the email encoding'
)
en[404416] = (
'Pride: What is topographic cretinism like?\n'
'Ihtiandr: Like yours, only topographic.'
)
en[404417] = (
'News lenta,ru:\n'
'"Bailiffs seized a cat and bananas for debts"'
)
en[404418] = (
'xxx:\n'
'- We\'ll need to reboot.\n'
'- Fine, fine, just don\'t close my open documents.'
)
en[404419] = (
'The CEO issued an order (verbatim):\n'
'"Heads of departments are to provide a list of employees who need daily exit from the enterprise, with reasons."\n'
'In reply received the full staff list with the reason: "daily ENTRY to the enterprise".'
)
en[404420] = (
'УУУ: listen, about your theoretical mechanics notebook. in the bibliography instead of the first recommended author there\'s "^-^". What kind of kawaii is that and what specifically should I get at the library? )\n'
'ххх: dunno, don\'t remember. wrote it a year ago. send a photo\n'
'//file transfer via roundabout ways\n'
'ххх: oh )\n'
'ххх: that\'s l-l\n'
'ххх: landau-lifshitz\n'
'ххх: imho, the pinnacle of kawaii )'
)
en[404421] = (
'OPK lesson (basics of Orthodox culture)\n'
'- Hello children! So... who\'s absent?\n'
'- God is absent!'
)
en[404422] = (
'Axxed:\n'
'Kiev. Night. Slightly drunk catching a taxi:\n'
'- Dorogozhichi for 25?\n'
'(t)- no, 30 and let\'s go\n'
'(me briskly) - ok 20\n'
'(t)- uh... well, at least 25\n'
'Let\'s go. A minute later the taxi driver:\n'
'- We bargained well, you and I...'
)
en[404423] = (
'lemb: how\'s your mood?\n'
'Tyapa: hi. kind of bad already...\n'
'lemb: what happened to you?\n'
'Tyapa: my wife upset me\n'
'lemb: who?!\n'
'Tyapa: my wife\n'
'lemb: whose?\n'
'Tyapa: mine\n'
'lemb: Mash, I\'ve known you for ages, but you never cease to amaze me! )'
)
en[404424] = (
'Zet\n'
'if you\'re going to court me - come on foot.\n'
'I know you, if you ride up to my house on a white horse, the horse will definitely shit right under the balcony'
)
en[404425] = (
'Grom: Something reminded me today of the joke about the nouveau riche\'s son calling dad yelling: Urgently deliver polar bears to the South Pole, I\'m failing geography\n'
'Lynx: what reminded you? the new rules? )\n'
'Grom: Yeah...\n'
'Grom: Seems someone flunked the EGE'
)
en[404426] = (
'xxx: Just got into the metro car, announcement "Next station Vasileostrovskaya. Attention! Some doors in some cars don\'t open!"\n'
'xxx: Petersburg roulette, damn...'
)
en[404427] = (
'comment on an article about artificial vision\n'
'\n'
'Don\'t take me for a bore, but "demon of wires", especially set off by commas, looks like something like "Power is planned to be provided, a thousand devils, from radio frequency radiation." :)\n'
'\n'
'(c) habrahabr.ru'
)
en[404428] = (
'xxx: Where\'ve you been?\n'
'yyy: My wife brought home a dwarf hamster.\n'
'yyy: The bastard slipped under the sofa and is hiding there. I spent a whole hour trying to lure the bitch out of there - no fucking way. Finally he got on my nerves completely, I lift this insanely heavy sofa - he\'s not there!!!\n'
'yyy: And it turns out he\'s sitting behind the door on his hind legs looking at me, with such a sympathetic-understanding look like: "What, woodpecker, feeling bad?"'
)
en[404429] = (
'xxx: You say Chelyabinsk people are harsh?\n'
'xxx: Here in Novosibirsk, one provider has a "Red Eyes" plan!'
)
en[404430] = (
'Riding for mushrooms from Yaroslavsky at 06:35 AM. September 1st. Next to me an old man picks someone\'s phone in his mobile for a long time, then calls: Hello, Sashenka? Happy Knowledge Day! Your voice sounds sleepy... get up or you\'ll oversleep everything! ... Half past six... half past six in the morning... In what ass? This is me, your grandpa! Sasha! Sasha???'
)
en[404431] = (
'she: where\'ve you been?\n'
'he: my son was born\n'
'she: wow, how old is he?'
)
en[404432] = (
'From an agriculture forum:\n'
'- eh, I remember you could get an adult cow for 5 thousand.\n'
'- for an hour or a night?'
)
en[404433] = (
'in useless_faq\n'
'p0tramp: What will happen on earth if suddenly all magnets stop magnetizing?\n'
'sanguisugus: There\'ll be a huge crash from them all falling off the fridges.'
)
en[404434] = (
'xxx: Opera 10 fulfilled a childhood dream!!!\n'
'xxx: Pictures on websites start opening from the bottom!\n'
'xxx: Too bad it\'s not needed anymore...'
)
en[404435] = (
'xxx:\n'
'Today at lunch, a call on the intercom.\n'
'My sister picks up the phone, and from there: "Open up. Evening newspaper."\n'
'She: "Why in the daytime?"\n'
'Silence...\n'
'\n'
'xxx:\n'
'They didn\'t call again, apparently the delivery guy\'s brain froze.'
)
en[404436] = (
'The director walks in: So, who\'s strong here?\n'
'Artist: What? Need to move something? No problem, I\'ll help.\n'
'Director: Need to transport a piano.\n'
'Silence, then laughter.\n'
'Artist: Well, if it\'s a piano, you need to slip straps under it.\n'
'Director: Have you carried one before?\n'
'Artist: No, saw it in a movie.\n'
'Programmer: And where to drag it from and to?\n'
'Director: From the second floor to the gazelle.\n'
'Programmer2: Well, from the 2nd floor to the gazelle we\'ll easily make it!'
)
en[404437] = (
'euphoria: Installed child-proof outlets so we wouldn\'t worry about the little one. An hour later my husband got zapped because he was curious how they work and stuck two spokes in the outlet.'
)
en[404438] = (
'from a forum:\n'
'On Bela my wife tried to talk to "deaf-mutes" in sign language (used to teach at a school for the deaf).\n'
'The dude naturally didn\'t understand a damn thing. So she tells me, "punch him in the ear, he\'s not deaf-mute at all".\n'
'\n'
'The dude squeaked "don\'t" and my wife joyfully says, "man, you owe us money for the miraculous healing"\n'
'Then he bolted and ran off. Probably went to share the joy of healing with his disabled comrades'
)
en[404439] = (
'Rouge: Read somewhere that the best cure for depression is lots of orange.\n'
'Rouge: Put a beautiful widescreen picture of a nuclear explosion on my desktop. I wonder how much optimism it\'ll bring me...'
)
en[404440] = (
'At Minsk station saw a homeless guy in a Gorizont TV box, with the ad slogan "Everything for life!". What a socially-oriented state, though!'
)
en[404441] = (
'xxx: Decided to prove probability theory, the lecturer took a coin and threw five heads in a row. xxx: Laughed, and with the words "you got the principle" put the coin back.'
)
en[404442] = (
'Daughter, 8, can\'t fall asleep. I suggest she "count 100 sheep"\n'
'she: I won\'t count unless you say "baa"!\n'
'what to do?\n'
'me: baaaaa...\n'
'she: one!'
)
en[404443] = (
'xxx: Just watched a funny scene.\n'
'xxx: Mom, addressing her two-year-old son: "Ew, poop! Spit out the poop! Mommy will give you a new one now"'
)
en[404444] = (
'nikita: no, our odmin\'s the toughest\n'
'nikita: on sysadmin day from morning to evening he walked around in jeans, a sweater and a theater beard\n'
'nikita: on an elastic band over the head\n'
'nikita: and that\'d be fine? but our odmin is a woman'
)
en[404445] = (
'YYY: Went for a walk yesterday. Walking, talking to Den on the phone. I approach a kiosk to buy beer, rummaging in my pockets for money, then I see - no phone (phone at my ear). I say to Den into the handset(!!!): "Den, I think I lost my phone..."\n'
'YYY: He answers: "Wait, I\'ll call it now, maybe we\'ll find it!"'
)
en[404446] = (
'xxx: Just wrote to a friend that 09.09 is my professional holiday. Tester\'s Day. The reply came: "Ok! We\'ll congratulate you with positive tests)))".\n'
'xxx: Damn, guys, I\'m scared..'
)
en[404447] = (
'In LJ:\n'
'xxx: Have fossilized mammoth excrement (presumably)\n'
'Might just be a stone. Trading for a Saab 900 pre-85\n'
'yyy: Have a 3 kg piece of iron.\n'
'Possibly it\'s a Saab 900 \'85. Trading?'
)
en[404448] = (
'xxx: And I taught my son to count very fast and without any force. We just played blackjack (matches were the money), periodically switching roles.\n'
'xxx: In a couple days my kid learned to count to 21 like a meteor.'
)
en[404449] = (
'From a women\'s forum:\n'
'\n'
'Reread the forum a lot, about all these men\'s tricks: deletes SMS, gets a second phone, puts passwords on the computer, takes the phone to the toilet and bath... just to hide his dirty deeds!!!\n'
'\n'
'But mine went further.\n'
'It all started when he bought me a little laptop, beautiful snow-white, made wifi in the apartment. "So you can be on the Internet more often" -- he said. At first I was happy like an idiot. Spent lots of time on the internet.\n'
'\n'
'Once I wanted to get into his computer. For prevention ))))\n'
'BUT!!!! THAT BASTARD redid everything there!!! I didn\'t even get it at first!! Everything was different!!\n'
'\n'
'One friend, whose husband is a sysadmin, said that my sweetheart installed Linux!!\n'
'When I asked him why he did that!?? He said Linux is more convenient for him!! But actually he did it on purpose so I couldn\'t use his computer AT ALL!\n'
'What\'s more, everything\'s different in his phone now too, says he CHANGED THE FIRMWARE!! Now it\'s totally unclear how to view SMS and made calls there, and I know for sure he\'s running around with his bimbos from work!! And I can\'t catch him now!!!\n'
'HELP!! WHAT DO I DO??!!!'
)
en[404450] = (
'ххх: now reading on the torrent "Damn, just 500 KB, add some speed pleeease! What, I\'m supposed to download 2 gigs for an hour???"\n'
'yyy: And so a new generation grew up...'
)
en[404451] = (
'xxx:\n'
'Once it got into my stupid head to get a perm.\n'
'Mom\'s reaction was ambiguous.\n'
'Sitting on a bench, looking at me, then at our poodle lying nearby.\n'
'- you know, you two look alike now.\n'
'- mom, he\'s lying with his ass toward us!'
)
en[404452] = (
'From the Star Wars site:\n'
'Happy September 1st, kids and Jedi!\n'
'\n'
'Happy Knowledge Day! Knowledge multiplies sorrow. Sorrow leads to hatred. Hatred leads to the Dark Side.'
)
en[404453] = (
'Kirill SheLLest:\n'
'Standing early Saturday morning at 7 AM at the tram stop, all kinds of people there, about five. A little boy 7-8 comes up and waits for the tram like everyone. One of the grannies, the most kind-hearted, comes up to the kid and in a sympathetic tone asks: "Where are you off to so early, grandson?!" To which the "grandson" in a sea-wolf tone: "To school, granny... to school..."'
)
en[404454] = (
'Got a job as a secretary. All day the boss walked around, addressing me formally and by name-patronymic. To my words that just Olya is fine he read a whole lecture about high culture in the company. By evening he asked for coffee, I brought it and then from behind the closing door comes a wild yell: "Fucking hell your coffee\'s hot! Dilute it, please!"'
)
en[404455] = (
'BEMBINI: the kid\'s growing up thrifty. peed. wiped up after himself\n'
'BEMBINI: with your clean shirt'
)
en[404456] = (
'<jaschur> sitting here listening to lolka feed her daughter\n'
'<jaschur> and she says: "you gotta swallow, Masha, swallow. good girls swallow."'
)
en[404457] = (
'xxx: "I\'ll try it and definitely report back" - the most popular last message of a forum thread'
)
en[404458] = (
'Vasilisa: came home from work, tired as hell! processed 20 kids in 6 hours\n'
'Alomorf: afraid to ask, what do you do?\n'
'Vasilisa: I\'m an animator )\n'
'Alomorf: damn! and I wonder why you sometimes start talking to people like they\'re kids\n'
'Vasilisa: you hit the nail on the head! =D my boyfriend\'s already getting offended sometimes\n'
'Alomorf: I have a friend, she\'s a dog person\n'
'Alomorf: she often tells me with this drawl: gooood giiirl\n'
'Alomorf: I liked it, then I heard her say it to the dogs (\n'
'Alomorf: most offensive is she gives them sugar, but not me ('
)
en[404459] = (
'<doob> Have you ever had something that bothered everyone, but you didn\'t want to throw it away because you thought someday it might come in handy for something?\n'
'<kol> Do kids count?'
)
en[404460] = (
'ZaoOoza: Was in Adler, walking past the road, a Mitsubishi drives by, with penguin airbrushing on its body.\n'
'ZaoOoza: The odmins drove by.'
)
en[404461] = (
'plaguebearer:\n'
'\n'
'Yesterday observed spherical Orthodoxy in a vacuum. Donetsk, evening, walking not far from the Kalmius embankment a shit-faced drunk guy - muttering something, staggering, grabbing every pole, his trajectory resembles a sine wave. Then suddenly stops, swaying, furiously crosses himself five times and marches on briskly and straight.'
)
en[404462] = (
'xxx: An infernal plan has matured: water the puddles the cat leaves behind with valerian.\n'
'xxx: Let him suffer too, the furry bastard'
)
en[404463] = (
'A couple on a bench, yelling over each other:\n'
'- Well, you didn\'t marry me!\n'
'- Well you told me to fuck off!\n'
'- That doesn\'t change the situation!'
)
en[404464] = (
'xxx: A humanitarian mindset is usually called the absence of a mathematical one.'
)
en[404465] = (
'xxx: Comrade, remember!\n'
'xxx: When coming up with a name for your son, you\'re coming up with a patronymic for your grandchildren!'
)
en[404466] = (
'xxx: even if you were told "forget everything you learned in school" - that\'s no reason to transplant your hands back onto your ass!!!'
)
en[404467] = (
'xxx: Still, a great man invented the kettle whistle!\n'
'yyy: The one who invents a whistle for the dumpling pot will become a great man.'
)
en[404468] = (
'George: all talk like "our country needs a strong leader, a firm hand" I associate with "I\'m a sheep, I\'m lost, where\'s my shepherd?"'
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
import re
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
