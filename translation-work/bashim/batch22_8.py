#!/usr/bin/env python3
import json, re, sys

BASE = '/home/hatch/workspace/translation-work/bashim'
START, COUNT = 800, 100

en = {}
en[404870] = (
'YYY: so you\'re working as a cashier now? how is it, liking it? ;)\n'
'XXX: yep... today when asked "what time is it?" on autopilot I blurted "19 rubles 22 kopecks, do you have a discount card?"'
)
en[404871] = (
'Overheard in a commuter train, after a recent football match:\n'
'- ... if our grandfathers had fought like our national team plays, the German-Japanese border would be in Chita!'
)
en[404872] = (
'xxx: I\'m not a loser. It\'s just that everything\'s adding up shitty.\n'
'yyy: a Soviet constructor set adds up shitty, and you\'re just fucked'
)
en[404873] = (
'xxx: want to become a psychic?\n'
'yyy: I foresaw this question, but no'
)
en[404874] = (
'kosta\n'
'it would be good to do pull-ups, but there\'s nowhere at home =(\n'
'\n'
'lil\n'
'on the windowsill!\n'
'from the other side\n'
'and it\'ll be motivation'
)
en[404875] = (
'FaTh3R: Trojan.Win32.Chifrax.d - 388 MB\n'
'BEs slyoz aka LazyFao: a Trojan elephant'
)
en[404876] = (
'Tatjana: Let them come and look at what a wreck I\'m sitting on. What you\'re on. The other guys.\n'
'Sanya: What\'s wrong? I\'m sitting on a good chair :)\n'
'Tatjana: Your chair, by the way, was stolen from another department. So from accounting\'s point of view - you\'re standing!\n'
'Sanya: Er...\n'
'Tatjana: Yes-yes. Standing. And you hold the keyboard in your hands because your desk... well, you get it.'
)
en[404877] = (
'PASKAL: decided to drink some cool cough pills\n'
'Darya: and how?\n'
'PASKAL: half an hour later I could barely breathe, like after a bite of some African hornet )\n'
'PASKAL: and guess what?\n'
'PASKAL: instead of calling an ambulance - went on a forum to ask what to do )\n'
'PASKAL: by the time they answered, I nearly died )\n'
'Darya: what did they advise? )\n'
'PASKAL: to call an ambulance...'
)
en[404878] = (
'he: The hardest thing is hiking in the mountains as a pair. Well, imagine, every day you wake up and see the same mug.\n'
'she: You know, many people live like that. And even raise kids. It\'s called family.'
)
en[404879] = (
'Gaz: My wife calls this morning - where are you, she says? I tell her - at work, she says - why didn\'t you sleep at home?\n'
'koly: And why didn\'t you sleep at home?\n'
'Gaz: I did sleep, she just didn\'t hear me come and go'
)
en[404880] = (
'Po6aT:\n'
'I understand you\'re working. answer when you can.\n'
'a classmate\'s hat looks like a disco ball. I have a wild desire to shine a flashlight on it, turn on the music, and have everyone around dance. want it so bad I can\'t sleep. how do I do it without getting clocked?'
)
en[404881] = (
'[wonderman] Frost, there\'s a task.\n'
'[wonderman] There\'s an office of 100 people, need to give them internet, while cutting their ICQ and big files over http.\n'
'[wonderman] One condition - don\'t configure anything. Tools - a molded 50cm patch cord.\n'
'[defrsto] hang a demotivating poster\n'
'[defrsto] whoever gets caught in ICQ gets fucked in the ass\n'
'[defrsto] by persuasion!\n'
'[wonderman] You\'re wrong. All you need to do - put the same dlink dir-100 on everyone\n'
'[wonderman] Under load it reboots every 40 seconds, files drop, ICQ bans for reconnects\n'
'[wonderman] And the main thing - the bosses are happy.'
)
en[404882] = (
'xxx: blasphemer!!! remove the picture to the devil\'s mother!!!'
)
en[404883] = (
'XXX\n'
'imagine, in 10 more years, the CPU cache will be in terabytes already )\n'
'YYY\n'
'and that scares me. imagine how ham-handed you have to be to write software that needs such a cache'
)
en[404884] = (
'Dr.Verhovtsev:At the warehouse there were 10 t(tons) of gooseberries, at 99% moisture. After a while due to shrinkage the gooseberries\' moisture dropped by 1 % (percent). How many tons of gooseberries are left at the warehouse?\n'
'AMD_mp: 10 tons\n'
'Dr.Verhovtsev: errr, why?\n'
'AMD_mp: because the invoice says 10 tons'
)
en[404885] = (
'Local: A snail can sleep up to three years.\n'
'Local: I wish\n'
'UNnameD: there\'s something of a snail in me\n'
'Local: An ostrich\'s eye is bigger than its brain\n'
'Local: there\'s something of an ostrich in you'
)
en[404886] = (
'xxx: When I retire, me and my friends will gather and finish all saved games in third Heroes'
)
en[404887] = (
'<Kaurka> I just realized what a communal apartment is\n'
'<Kaurka> it\'s when there\'s a 40-gig drive and 4 people on 1m computers'
)
en[404888] = (
'wb: remember, I was worried that he\'s always yelling that he\'ll jump off the balcony?\n'
'wb: I quit that when I found out he lives on the 1st floor'
)
en[404889] = (
'<@Monster> People at work told me I shouldn\'t have named the function DrawChildren\n'
'<@Monster> because when you pronounce it fast...'
)
en[404890] = (
'Den: Yeah right, in my student years my probability theory teacher lost a pass/fail to me at 21.'
)
en[404891] = (
'xxx: Don\'t f*ck my brain, you\'re scaring the cockroaches!'
)
en[404892] = (
'xxx: And I\'ll have a cat today!\n'
'yyy: Jealous of you.\n'
'yyy: I have pasta again...'
)
en[404893] = (
'<Гексоген> when I was little I went fishing for the first time, in Siberia, grandpa cooked fish soup\n'
'<Гексоген> and I kept wondering where he got dill in the taiga, it was dark after all\n'
'<Гексоген> and those were mosquitoes floating there )\n'
'<Гексоген> by the firelight they looked like dill...'
)
en[404894] = (
'Lena: Nastya, let me try on your pants\n'
'Me: you\'ll drown in them)\n'
'Lena: drowning in pants... What a ridiculous death!'
)
en[404895] = (
'1: So tell me, what\'s the difference between a switch and a hub?\n'
'2: Look. Imagine a network as a regular chat.\n'
'2: In the chat there\'s one Vasya (that is you, aka the sender)\n'
'2: and four Lenas (that is the other users)\n'
'2: A switch - that\'s when you send in private to your one beloved Lena "I love you", she replies with a smiley and all is well.\n'
'2: And a hub - that\'s the public of this chat. You write "Lena, I love you!!!", and in reply you get three "Meh"s carrying no meaning and one "Who the f*ck are you loving there???!!!"'
)
en[404896] = (
'from LJ:\n'
'Were looking for the cause of the catastrophically large percentage of broken alcohol products during delivery by rail from a certain direction. Seemed like they loaded it carefully, but it arrived in god knows what shape. A cardboard-glass salad in a wine-vodka sauce. Finally figured out what was going on. The rail workers, bastards, were pushing cars with booze down a hill, they rolled downhill with a breeze and with an appetizing rattle slammed into empty cars pre-arranged on the tracks. As a result everything inside the car with booze broke and crumbled, and the rail workers, bastards, stood in line with buckets at the corners of the car. After the rivers, and then the streams, of alcohol dried up, the car went on its way.'
)
en[404897] = (
'DEAD: Went to visit relatives today, they have my cousin brother, still little. Well, they asked me to play with him, gave toys, etc. Many played some music when pressed. I picked up some donkey, pressed... I\'LL BE DAMNED!!!! The title theme from third Heroes!!!'
)
en[404898] = (
'gonzo_man: we had two friends at school: a hacker and a metalhead, inseparable\n'
'ass_itch: Chuk and Gek?\n'
'gonzo_man: Freak and Geek'
)
en[404899] = (
'xxx: Time to bail to Sweden, they have the highest living standard... \n'
'xxx: And suicides\n'
'yyy: People climb to their living standard and jump?'
)
en[404900] = (
'Katya: Dim, how do you and Natasha solve the computer-sharing problem?\n'
'DiSel: we put a cartoon on the TV for the kid - and the computer is free for us'
)
en[404901] = (
'LJO: Friend, help, what to do?!!!!! Today my wife made me happy. She says: "Today I\'ll make mushroom soup, you\'ll lick your fingers!" I\'m already in a lifted mood waiting for a tasty dinner, and while my wife conjures in the kitchen I sit down at the computer. I see Mozilla open. Besides tabs with recipes I discovered a google query "mushroom poisoning symptoms". I\'m kind of shitting myself. Refusing would be awkward somehow, she\'s trying so hard... She says it\'ll be ready soon...\n'
'Dmitry: Well at least write back afterwards, after you eat...'
)
en[404902] = (
'xxx: they say if both parents want a son to be born, and a daughter is born - she grows up such a brutal tomboy... And vice versa, if they want a daughter and a son is born - he often grows up a wimp, a crybaby and a mama\'s boy.\n'
'xxx: so\n'
'xxx: my parents always wanted a dog...'
)
en[404903] = (
'sirbug: Fairy tales have huge educational value\n'
'sirbug: Say "Hansel and Gretel" teaches kids the following truths\n'
'sirbug: Don\'t go into unfamiliar houses, don\'t trust unfamiliar old ladies\n'
'sirbug: Be careful - one day your parents will take you into the forest and leave you to certain death'
)
en[404904] = (
'xxx: Today I saw a student, he was standing at the bus stop waiting for the trolleybus\n'
'xxx: Took out a cigarette, lit up, smoked half a cigarette, the trolleybus pulled up\n'
'xxx: He looked at the half-smoked cigarette, at his transit pass... \n'
'xxx: And stayed to wait for the next trolleybus'
)
en[404905] = (
'Zhaiz: No, this time I really fell in love. Drew hearts all over the desk.\n'
'1\'st: Idiot, you\'ll be scrubbing it forever.\n'
'Zhaiz: no problem, just wipe the dust.'
)
en[404906] = (
'he: imagine you lived in two dimensions\n'
'he: how would you look at the third?\n'
'he: you only have length and height, you don\'t even know width\n'
'she: listen, I like it )'
)
en[404907] = (
'Shurik_88: Happy owners of Toyota Vista move up to BMW 7.'
)
en[404908] = (
'xxx: hi. you don\'t happen to know the fate of our WiFi? I\'ve been unable to connect my laptop for three days\n'
'\n'
'yyy: I know\n'
'yyy: it\'s at school\n'
'yyy: Medvedev came to this school'
)
en[404909] = (
'skinny bitch: why isn\'t our ftp working?\n'
'leshiy: Technical robots\n'
'skinny bitch: they\'re bitches\n'
'leshiy: Who?\n'
'skinny bitch: these robots, the technical ones )'
)
en[404910] = (
'XXX: what will you be for halloween?\n'
'YYY: a tech support employee ('
)
en[404911] = (
'boss: you busy right now?\n'
'me: no, while the render\'s going.\n'
'boss: I brought a new girl here, for an internship. show her 3DMax real quick, in like 5 minutes, while the render\'s rendering.'
)
en[404912] = (
'xxx: want to sleep. how to wake up?\n'
'yyy: go, buy 0.5 of cola, two spoons of black coffee in it and drink\n'
'xxx: poor organism\n'
'yyy: there\'s one more tip, but it\'s more inhuman. I\'m afraid to even recommend it to you\n'
'xxx: recommend, recommending is nothing, the main thing is deciding whether to use it or not\n'
'yyy: go to bed on time'
)
en[404913] = (
'On a forum:\n'
'Milyochka (14:45) : I want to tell you a story (...) . Everything\'s so bad :(\n'
'Mila (15:00) : Milyochka, damn, the story\'s just like mine!!! For real!!! Plus for you! Agree!! You smart girl.\n'
'Admin (15:23) : Fine, nobody sees your IP, but you could at least change your nick so you don\'t give yourself away talking to yourself ;)'
)
en[404914] = (
'xxx: I figured out why in brutal death every song starts with the vocalist\'s drawn-out growl - so you know where the new song starts'
)
en[404915] = (
'***: so did you go to the concert?\n'
'lavery: of course. lost almost 2 kg in 2 hours (\n'
'***: quite a diet )\n'
'lavery: that\'s no diet. that\'s fucked. lost my third laptop this year ('
)
en[404916] = (
'Sophia: something\'s wrong with me...\n'
'DoubleDJ: what\'s up?\n'
'Sophia: oh, that\'s not for you..\n'
'DoubleDJ: meaning, for me you\'re all good? )'
)
en[404917] = (
'Symposium: here, found you a cheesecake recipe\n'
'real_max: very, f*cking, thanks!!!\n'
'real_max: without even looking at the recipe, I can firmly assure you that in the whole house the ingredients I have are an egg (one piece) and salt (about a kilo)\n'
'real_max: and that\'s a shitty cheesecake, believe me'
)
en[404918] = (
'xxx: Today I scoped out speakers in the computer store, a 5.1 system, a chic sub, the speakers themselves are my height, the sound is just amazing, just cool!\n'
'xxx: Tomorrow I\'ll go look at them again'
)
en[404919] = (
'Ran^: want me to buy you juice?\n'
'L: no\n'
'Ran^: and a chocolate bar?\n'
'L: no\n'
'Ran^: and buy anything at all?\n'
'L: no\n'
'Ran^: Now we\'ll check the rule of four no\'s. Do you like music?\n'
'L: No\n'
'L: such a rule'
)
en[404920] = (
'on one forum about robot vacuums:\n'
'\n'
'xxx: A friend of ours in Italy had one of those! Burned out in a week! Because the pet cat in the owners\' absence would climb on it and ride it!'
)
en[404921] = (
'xxx: I\'ve come to terms )\n'
'xxx: with this world\n'
'yyy: and who\'s bigger?'
)
en[404922] = (
'Irishka:\n'
'well, what I can - I\'ll help you with\n'
'but I won\'t write your whole thesis for you\n'
'\n'
'dynamic_by:\n'
'well, that goes without saying \n'
'\n'
'dynamic_by:\n'
'I\'ll make the title page myself'
)
en[404923] = (
'<cogniter> A drawback of unix is that it can\'t execute a command longer than 2 gigabytes from the command line.\n'
'<strafer> a real drawback, I\'ve hit that problem a hundred times\n'
'<cogniter> strafer: don\'t sleep on the keyboard'
)
en[404924] = (
'Voland: Dear Lisa, I understand that now it\'s late to write you something, since a chasm has already formed between us, which perhaps can no longer be crossed.\n'
'Voland: Probably I need to put a period at the end. Could you return the book on Turbo Pascal? If not - state the reason.'
)
en[404925] = (
'she: my competitiveness is that I can be happy with you without a stamp in the passport'
)
en[404926] = (
'he: put on something easily torn\n'
'she: a newspaper?'
)
en[404927] = (
'Hett: At work the door closes on three padlocks. All the locks look the same from the outside, keys too. I\'m sick of playing Fort Boyard every day :('
)
en[404928] = (
'Natalia: sometimes I feel like you\'re weird\n'
'Natalia: can you answer the question normally and briefly instead of bloviating?\n'
'Darth Vader: that\'s an occupational hazard. if I give a clear and brief, and most importantly correct and unambiguous, answer to a question, they\'ll pester me to death with questions'
)
en[404929] = (
'Scratch: I figured out why all true admins wear beards )\n'
'Poha: And why\'s that?\n'
'Scratch: So that when you sleep during work hours, no compromising keyboard traces stay on your face )'
)
en[404930] = (
'Elvira: And the prince called me!!!!!!!!!!!!!\n'
'lanquu: wrong number?'
)
en[404931] = (
'xxx: Yesterday I was really worn out during the day, so I went to bed at half past eleven instead of 2-3 am like usual.\n'
'xxx: In the morning I woke up at a quarter to eight and waited 15 minutes for the alarm clock.\n'
'xxx: Funny state... the organism is trying to figure out where it got cheated.'
)
en[404932] = (
'Yascherko: bought a sweater in a black-gray-white-reddish speckle. in it I can grab cats of _any_ color without fear!'
)
en[404933] = (
'xxx: November, time to hand in labs. Tomorrow I\'ll go to the polytech\n'
'yyy: You graduated from it 5 years ago already!\n'
'xxx: Well I\'m going to accept them'
)
en[404934] = (
'xxx: the other day I bought myself an energy-saving bulb, came home, connected it and it doesn\'t light up\n'
'yyy: that\'s right, it\'s saving energy.'
)
en[404935] = (
'xxx: Has anyone actually read the license agreement to the end? \n'
'xxx: Do they get married after all?'
)
en[404936] = (
'From a forum: \n'
'\n'
'> There have often been cases when a payment was made via the cashier\'s vein to the wrong number!!!\n'
'\n'
'Yep. "The cashier set aside the syringe and blissfully closed his eyes. The next payment ran through the cashier\'s vein."'
)
en[404937] = (
'Architect: What is the sun busy with?\n'
'Gothess: The sun? The sun is setting behind the horizon and gently illuminating my room with pink light...\n'
'Gothess: And I\'m carving up a pig carcass.'
)
en[404938] = (
'samit: Standing at the bus stop, waiting for my dad, mad as a dog - a very unpleasant event happened then. And then a guy walks by: "Hey, hear me, give me a cigarette" I don\'t react.\n'
'He again: "Hey, give me a cigarette!" I turn around with a cigarette in my teeth: "I don\'t smoke". Leaving offended: "Damn you, Tyson, f*ck".'
)
en[404939] = (
'Syorin: Going outside today, I suddenly realized that global warming got globally colder overnight...'
)
en[404940] = (
'xxx: got a promotion at work today\n'
'yyy: and who are you now?\n'
'xxx: now I work on the second floor )'
)
en[404941] = (
'xxx: for Halloween I\'ll dress as a blue screen of death'
)
en[404942] = (
'username: In this internet of yours you can\'t tell who\'s joking and who\'s really an idiot.'
)
en[404943] = (
'--Irinka--: Trouble with the car - parts take long to arrive, need to change the suspension.\n'
'AleksannYurna: Oh these suspensions! \n'
'AleksannYurna: D\'Artagnan, for instance, rode to a neighboring kingdom for them.'
)
en[404944] = (
'polkovnik-isaev: yeah it\'s a PIECE of the program\n'
'arsenicum: polkovnik-isaev: you\'ve been deceived. it\'s not a piece of the program'
)
en[404945] = (
'HUNTon: just rewatched the Matrix\n'
'HUNTon: got what Neo\'s success is about\n'
'HUNTon: he has better ping )'
)
en[404946] = (
'Morbid_Dezir: My whole life flashed before my eyes after a metal spoon fell out of a mug and bounced toward the open system unit...'
)
en[404947] = (
'xxx: our cat loves boxes\n'
'xxx: gave him one, but it\'s too small for him, he can\'t sleep in it comfortably\n'
'xxx: dad watched the cat suffer for a long time and the other day pleased the cat \n'
'xxx: "I\'ll find you a big box at the dump with my paycheck"'
)
en[404948] = (
'xxx: When the soul asks for a celebration...\n'
'yyy: ...the wallet begs for mercy! )'
)
en[404949] = (
'xxx: who knows what happens after death???\n'
'yyy: viewing your walkthrough stats\n'
'yyy: getting achievements\n'
'yyy: main menu'
)
en[404950] = (
'WooBler: what\'s with Anyka? came all in tears, the folks are confused - that\'s not like you, and she only wails and moos something unintelligible.\n'
'Minsc: Mademoiselle announced that I have a lover.\n'
'WooBler: why would that be?\n'
'Minsc: Came back from a business trip all in clean clothes.\n'
'WooBler: and?\n'
'Minsc: What do you mean "and?" This child of progress, you see, didn\'t believe that I just took everything and hand-washed it in the hotel bathtub, then begged an iron from the housekeeper and pressed it. That supposedly nobody washes like that anymore. And since the laundry\'s clean - I must have a lover. The fact that I wash right in the river when fishing isn\'t an excuse, because there\'s definitely no washing machine on the river and I\'m under supervision there.'
)
en[404951] = (
'Sliva: You have circles under your eyes..\n'
'Myxa: That\'s eyeshadow!\n'
'Sliva: Did it crumble?\n'
'Myxa: No, it settled...'
)
en[404952] = (
'Overheard conversation between mother (m) and father (f) about the younger brother:\n'
'M: Today at work they told me they saw Kolya buying condoms at the pharmacy. What should we do?\n'
'F: Rejoice that it wasn\'t a pregnancy test...'
)
en[404953] = (
'Stranger:\n'
'a guy on a forum has a signature\n'
'Stranger:\n'
'What is this sleepless night worth?\n'
'What is this dreamless day worth?\n'
'What is this endless loneliness worth\n'
'And life without love...\n'
'Stranger:\n'
'it\'s 400 rubles a month for WoW on the official servers'
)
en[404954] = (
'Rakshas: it seems dads should raise kids. Then they\'ll grow up strong and hardy\n'
'Rakshas: and independent\n'
'Rakshas: if they survive\n'
'Rakshas: because to survive from birth in an atmosphere of healthy indifference - you need a monstrous will to live'
)
en[404955] = (
'paul: Such a question. We somehow discussed that it\'d be good to increase file sizes on the site.\n'
'paul: you said then that you don\'t recommend it\n'
'AK: yeah\n'
'AK: long connects\n'
'paul: can you remind me what the reasons were?\n'
'AK: well imagine - a server is like a box with holes that sausages stick out of. There are lots of sausages in the box, but the number of holes is limited. And then 100 guys come and demand sausages. And they\'re long ones. And the long-sausage guys sit at the server and slowly gnaw their sausage at their full possible speed. And around them angry users crowd who didn\'t get sausages.\n'
'AK: sorry, I\'m hungry.'
)
en[404956] = (
'xxx: Just walking on business, an Oka drives by with a rag sticking out of the open gas tank\n'
'yyy: A Molotov cocktail, damn...'
)
en[404957] = (
'Denis (IT): Cat, question, say you\'re an admin. At a new firm and they ask you to run the network and set everything up, what would you ask for first of all?\n'
'\u00adNight Cat: An advance.'
)
en[404958] = (
'<xxx> I witnessed the year before last how a cool black jeep on Sokol drove along the tram tracks, and when it stopped opposite a tram coming the other way, started honking at it furiously.'
)
en[404959] = (
'xxx: Eating cookies right now\n'
'xxx: There\'s a date, consume before November 26, 2019\n'
'xxx: Thinking - damn, Vault-Tech cookies. Straight up a 10-year shelf life for the nuclear apocalypse.\n'
'xxx: Then I look at the production date, October 27, 2019\n'
'xxx: I\'m scared of something )'
)
en[404960] = (
'xxx: our house\'s plumbers took flashlights and went somewhere, the apartment\'s cold!\n'
'yyy: Plumbers, when it gets cold, go to warmer lands. Didn\'t you know?\n'
'yyy: And the flashlights are needed because they walk both day and night.'
)
en[404961] = (
'MorpheuS: what date is the revolution?\n'
'Lexx: November 7\n'
'MorpheuS: in tsarist times we\'d have been exiled to Siberia for such correspondence'
)
en[404962] = (
'Raus: Happiness around - as much as you want. Just bend over and pick it up.\n'
'AllekS: How true - to each his own!'
)
en[404963] = (
'Jekyll:\n'
'What harmless names we have...\n'
'Rockets "Topol". Just heard about a successful launch from a submarine of the intercontinental ballistic missile "Sineva"!\n'
'\n'
'Serenkiy Iozhik:\n'
'so that\'s what the expression "lit up with sineva" means...\n'
'We also need an anti-personnel mine "Nezabudka"\n'
'\n'
'Serenkiy Iozhik:\n'
'the field was strewn with nezabudkas... poetic :)'
)
en[404964] = (
'xxx: Sanya was recently discharged from intensive care. Now he quit smoking and stays home in windy weather :)\n'
'yyy: What happened?\n'
'xxx: He was smoking at a bus stop at night, a strong wind was blowing. A bunch of gops walked by. Well he threw the butt in the trash can, and ended up in intensive care...'
)
en[404965] = (
'xxx: Are there people here who can imagine the infinity of the universe for a long time? )\n'
'yyy: There are. They can. But it\'s scary :)'
)
en[404967] = (
'Drakula: anyone have a strategy for lineage\n'
'DeBugger: Drakula> I have an action-protractor. Need it?'
)
en[404968] = (
'she: Darling, let there be at least a few months, some mystery in me...\n'
'he: A mystery lasts a few months if the couple doesn\'t want an ultrasound to determine the sex! )'
)
en[404969] = (
'xxx: and the smell of triple cologne is forever associated for me with Soviet tape recorder equipment'
)
en[404970] = (
'<Iron_> here\'s what I\'m curious about\n'
'<Iron_> a bed linen store - Spal Spalych\n'
'<Iron_> so what would these creatives call a plumbing fixtures store?'
)

expected_ids = [404870 + n for n in range(101) if n != 96]  # 404966 missing in source
def main():
    src_lines = open(BASE + '/chunks/chunk_22.jsonl', encoding='utf-8').read().splitlines()
    recs = [json.loads(l) for l in src_lines[START:START+COUNT]]
    got = [r['id'] for r in recs]
    assert got == expected_ids, 'ID order mismatch: %s' % (got[:5],)
    assert set(en.keys()) == set(expected_ids), 'missing/extra ids: %s' % sorted(set(expected_ids) ^ set(en.keys()))
    bad_nl = [(r['id'], r['text'].count('\n'), en[r['id']].count('\n')) for r in recs if r['text'].count('\n') != en[r['id']].count('\n')]
    if bad_nl:
        print('NEWLINE MISMATCH: %s' % (bad_nl,))
        sys.exit(1)
    bad_tok = []
    for r in recs:
        for t in set(re.findall(r'<[^>\n]*>', r['text'])):
            if t not in en[r['id']]:
                bad_tok.append((r['id'], t))
    if bad_tok:
        print('TOKEN MISMATCH: %s' % (bad_tok,))
        sys.exit(1)
    out = open(BASE + '/out/chunk_22.jsonl', 'a', encoding='utf-8')
    for r in recs:
        out.write(json.dumps({'id': r['id'], 'ru': r['text'], 'en': en[r['id']]}, ensure_ascii=False) + '\n')
    out.close()
    n = sum(1 for _ in open(BASE + '/out/chunk_22.jsonl', encoding='utf-8'))
    print('appended %d, out now has %d lines' % (COUNT, n))

main()
