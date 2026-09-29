# -*- coding: utf-8 -*-
"""Batch 9 for chunk_30: source-order records 801-900 (IDs 412886-412985)."""
import json, re, subprocess

SRC = 'chunks/chunk_30.jsonl'
OUT = 'out/chunk_30.jsonl'

lines = open(SRC, encoding='utf-8').read().splitlines()
recs = [json.loads(l) for l in lines[800:900]]
assert len(recs) == 100, len(recs)

EN = {
412886: "xxx: My grandpa will live another hundred years! Yesterday on his laptop's playlist fresh porn showed up...",
412887: "xxx: are there flash drives with a display?\nyyy: yeah, it's called a player",
412888: "ххх: One girl at our place fell asleep during the lecture where they were deciding pass/fail grades for the subject. And when she woke up, she learned she'd been given a fat pass with the motivation - if she's sleeping, she must know everything.",
412889: "xxx: watching a porn video on some site. The plot: in an ambulance driving along, the \"patient\" is having sex with the nurse.\nFirst comment under the video: \"damn, what smooth roads\".",
412890: "xxx: I spent a whole week forcing my husband to start the toilet renovation!!!\nyyy: and he?\nxxx: on Saturday he closed the toilet, hung a sign «Toilet closed for renovation» and left for the garage!!!!!!!",
412891: "xxx: damn! I can't find the case\nххх: for the body\nууу: a case for the body? is that what clothes are called now?",
412892: "xxx: I'm not a jerk, but you're really lagging. you need to have your skull opened and the pattern of your convolutions redrawn on paper, and based on the drawing make car tire treads. the slippery road problem will be solved once and for all",
412893: "Jenny Gray: the cat locked himself in my room today. screamed his head off and ruined the door until my parents came home from work and freed him.\nDataGreed: needed to poop?\nJenny Gray: no, just got tired of sitting in the room\nDataGreed: there's a computer there\nDataGreed: cats are dumb",
412894: "xxx: night shift, therapy ward\nxxx: sitting at the post, 2 AM, calmly playing dead space\nxxx: a granny suddenly emerged from the darkness\nxxx: quietly\nxxx: wrapped in a sheet\nxxx: came up from behind and quietly asks \"where's the toilet here?\"\nxxx: I almost made a toilet at my workplace!!!",
412895: "<%BOT> [Question]: The axis along which the \"x's\" jump (8 letters, 1 word)\n<&Faithlezz> linux\n<&Faithlezz> no?",
412896: "...Smoking in hotels, cafes, nightclubs, and also shooting films and TV shows with smoking characters is prohibited by the document from 2015...\nxxx: They won't shoot any more Sherlock Holmes movies? And can they at least show the old ones with smokers? And besides - sometimes it's necessary for the plot. Always some stupid extremes.\nyyy: For smokers there will be special stores, like sex shops. They'll sell magazines and films where people do THAT in opaque bags.",
412897: "Support request:\nSubject: COMPUTERS WON'T TURN ON AFTER THE WINDOW BARS WERE TAKEN OFF",
412898: "xxx: Sold a small drilling machine, in the morning a client calls - Your machine doesn't work at all. I answer, bring it to our service we'll figure it out.\n5 minutes later a call from the same company but a different person, like Mikhail, we bought a machine from you and can't find drill bits for it anywhere. I tell him, someone from you already called and you already broke it.\nHe in response - Broke??? who??? we have nothing to work with! No drill bits!!! how did we break it???\n...barely held back from guffawing into the phone",
412899: "slow_cheetah*: our fucking praying mantis won't eat anything\nslow_cheetah*: Pasha bought him crickets\nslow_cheetah*: put one in with him\nslow_cheetah*: and he doesn't notice it\nArghBuzz: Maybe he's blind?\nslow_cheetah*: maybe he's a vegetarian?\nArghBuzz: Maybe he's a mantis?\nArghBuzz: Sits\nArghBuzz: and prays\nArghBuzz: and you keep feeding him food\nArghBuzz: HE'S FASTING, FUCK",
412900: "vla: This morning I was thinking: first I need to shave, then go get photographed.\nvla: Put the razor on charge, fucked off to get photographed, came back, shaved, and realized I parallelized wrong.",
412901: "comment on an article about a melodrama in which N. Portman is to play a woman who stopped aging:\n\nAt what age does she stop aging? If in her youth, nothing original. But if she stopped aging at 80, that'd be fresh and interesting. Watch a granny with a bunch of old-age ailments living on her pension. First the children hope for the apartment after her death, the children die, the grandchildren start waiting. The pension fund goes crazy from the payouts.",
412902: "xxx: what position do you like best?\nyyy: lotus",
412903: "ххх: the console asks: \"download 250MB of packages? [Y/N]\"\nххх: and my internet is slow...\nххх: WHAT THE HELL ARE THE RUSSIAN 'N' AND THE ENGLISH 'Y' DOING ON THE SAME KEY???",
412904: "xxx: well I'm going to a wedding today )\nxxx: so I'm not working )\nyyy: Whose wedding?\nxxx: Lenin's :)\nyyy: But he died long ago!",
412905: "[OZK]ne4eHbKo: I love the bathhouse, BUT CAN THEY SOMEHOW REMOVE IT FROM THE SUBWAY?!",
412906: "ххх: My better half baked a \"Rotten Stump\" cake for my birthday.\nххх: As if hinting",
412907: "саша: I generally think that propping up trees with wooden sticks is like making crutches out of people's bones!",
412908: "Newspaper ad:\n\"Computer club needs a system administrator with knowledge of the \"Linex\" program, salary 20000 rub.\"",
412909: "me: still, the Russian mind can't be understood\nme: the neighboring office - a travel agency - got robbed, so the owner instead of installing an alarm invited a priest to bless the office",
412910: "Андрей: I wonder what would happen if you pour valerian on a dog and sic it on a cat?..",
412911: "xxx: got a message from the trip: \"please check your email. your departure time has changed\". I check my email. there's a letter on it: please check your email. your departure time has changed.\nxxx: what should I do?",
412912: "Push: Our office admin is going crazy. Every time the cleaning lady empties the trash bin, he quickly writes something on a piece of paper and throws it in the bin. We decided to check what he writes... \"first one, fuck you\".",
412913: "Rambler delights again\n\nBiochemistry specialists from the Netherlands synthesized possibly the strongest artificial human skin of existing analogues. According to scientists, even bullets can't pierce their new invention.\n\nFirst comment:\nThat's it, ARMAGEDDON, humanity will die of appendicitis.",
412914: "Miss Lady: Screw this job! Let's go home, lie in bed!!!!\nSlava.yo: Let's go!\nSlava.yo: Should I go ask for time off?\nMiss Lady: Noooooo.... They won't let me go.... (((((",
412915: "xxx: Sleep through breakfast, leave lunch for dinner, and give dinner to the enemy",
412916: "Асе: When you're drunk, you realize how many people are waiting for your call right now :)))\nАсе: that's why I called",
412917: "CKuB: Got dragged into the clinic. A granny who squeezed ahead of the huge queue \"just to get a stat ticket\" at the \"stat. ticket issuance\" window blew the aforementioned queue to hell.",
412918: "xxx: Harsh everyday life of the factory.\nxxx: Walking through the shop today and hear an electrician proving to a guard that if he told him to fuck off and said \"please\" while doing it, it doesn't count as swearing.",
412919: "Thomas XIII: How can you trust the mail if even in online stores the shipping notification contains detailed instructions on \"how to properly swear at the post office if your package didn't arrive\"?",
412920: "xxx: saw in the complaint book of one of the cinemas. \"3D doesn't smell, cheating is not good\".\n\nAnd, actually, there's the administration's reply: \"Mr. YYY was in a drunken state, and we couldn't convince him he was watching the film in a volumetric image, we tried for a whole hour\".",
412921: "Nuklid: Give wise advice, where can I find a plastic hemisphere, 50mm to 150mm, preferably white\nBigtatar: emmm....\nВалёк: everyone answers like that\nBigtatar: well only if you take apart some chandelier\nВалёк: I'm making a chandelier =(",
412922: "xxx: I have a classmate\nxxx: They wanted to expel him from university for study debts, there was already an order. He got really upset, finally realized he's a rare idiot and decided to reform.\nххх: Decided to go to Tibet to the monks, so they'd teach him diligence and humility. He already told everyone about his plans, was getting ready...\nyyy: Did he leave?\nxxx: no, they didn't expel him :D",
412923: "kiLstrom: Here's a question that's long tormented me. Why if an artist painted a fucking awesome painting, does he always sign at the bottom like a chicken with its foot?\nromanvzromanvz: try signing beautifully with oil paint, you'll be quite surprised ;)\nxyasse: I tried. they kicked me out of the museum.",
412924: "_Lanze_: Yesterday in minecraft I made a model of my workplace. Got really scared when a stray sheep walked into the server room.",
412925: "Conversation on FB\n\nXXX: \"Habitat\" is on Channel One. They're telling about online store fraud. One of the heroines ordered cosmetics, then discovered her phone and other personal data got indexed by Yandex. She calls the store and hysterically: \"I don't understand HOW MY DATA ENDED UP IN THE COMPUTER\", and also gets offended that the operator doesn't understand her..\n\nYYY: She's an obvious idiot. Everyone knows it's enough to buy a new monitor for all old data to disappear",
412926: "LeeRoy: this morning a garbage truck under my windows tried to transform into a decepticon. don't know how successful, but the sound was insane, of course.",
412927: "Mr. Muscle: this is a madhouse...\nMr. Muscle: went to HR, need a copy of my work record book\nMr. Muscle: the lady says: we've started saving paper, we account for every A4 sheet, so to get a copy of your work record book - write an application\nMr. Muscle: ON AN A4 SHEET, FUCK!!!!!",
412928: "ххх: How all these myths piss me off, everywhere it's the same thing...\nххх: Look, right now without thinking I'll write something just as PATHETIC...\nххх: The legend of the brave turtle.\nххх: And out of the pipe came a mustached plumber, and smashed the stone, and ate the spotted mushroom, from which he became twice as big, and plucked the flower that grew from the stone, and he began to throw fire. From the slabs smashed by his head gold poured in all directions, deafening the surroundings with squeaking and ringing. And only one little turtle wasn't afraid of the giant, hid from his paws in her shell and lay down in his path. With one touch the giant launched the brave turtle into a distant flight. With delight and surprise the beasts watched as, bouncing off the crumbling walls, the brave turtle knocked the giant off his feet, took away his terrible fire, folded it in half and threw it underground. It all happened so fast that no one had time to figure anything out.",
412929: "ххх: Well he's really a dumbass! He's been selling his junker for half a year.\nууу: So what, dad sold his for a year until he dropped the price to minimum )\nххх: Fuck, but he posts a sale ad and then doesn't answer unknown numbers!",
412930: "xxx: Night. Silence. Stars. Wind noise. \nxxx: And a roar from the megaphone of a DPS car \"ARE YOU FUCKING KIDDING, SIGNALING IN A RESIDENTIAL AREA!!!\"",
412931: "xxx: And do good people drive the wrong way in Mercs at 06:30?\nyyy: only in the trunk.",
412932: "Евгений: I'm a dumbass\nЕвгений: Explained to my brother that a big apple is called \"an applishche\". He explained it to our parents... :D",
412933: "xxx: was smoking on the balcony, singed the rug\nххх: now the rug will rat me out :(",
412934: "Full realism of computer games will be achieved when in computer puzzles little pieces get lost!",
412935: "ххх: I like that, when offering to take a photo with a boa constrictor, everyone says the snake (or even \"snakey\") doesn't bite. Of course it doesn't bite. It strangles, breaks your neck and swallows you whole. So don't worry.",
412936: "Justadreamer: Vera is in shock, her Dasha is dating a boy who plays bass in the band \"Marginal Coitus\", and one of their albums is called \"Lathe Paralysis\", and another - \"Seminal Duct\". Vera screamed into the phone and cried, and I laughed so hard I slid off the couch. I feel ashamed now)",
412937: "lil: and I woke up to rap again, all broken. If only this neighbor kid would already go to school...\nlil: monotonous boom boom and a guy saying something without changing intonation\nlil: they should at least read something useful like incantations. Turn it on, and it reads you one for a stye.",
412938: "ххх: You say you love me only when you feel bad, or after sex...\nххх: Such bad sex? :(",
412939: "Me: I have a romantic dinner today)\nСанечка: What are you cooking?\nMe: spaghetti carbonara\nСанечка: Oh wow\nMe: told the girl I'm a master of this dish, it turns out great for me... and now I'm searching the net for how this crap is made",
412940: "Comment on an acoustic cable priced at 220 thousand rubles per meter.\n\nI recommend it to everyone! After installing this cable I instantly felt improvement in the characteristics of both my own sound system and the neighbor's boombox that had been bothering me for a long time. Besides, my adhesions dissolved, my wife finally got pregnant, my mother-in-law has been silent for two days, and within a 200-300 meter radius (by my estimates) the political situation in the country improved. Dear manufacturers, please raise the price, I want even more impressive results!",
412941: "Шанхай: A week ago my wife insured me for 500,000. Didn't even ask me.\nTwilight_Sun: I'd be on guard.\nhelxen: Shanghai, hasn't the food acquired an unfamiliar taste?",
412942: "xxx: The stupidest waking up in my life...\nxxx: Riding a train at night. Lying on the lower berth with my head to the window, the neighbor above lying with her feet to the window.\nxxx: I wake up in the middle of the night because cucumbers are falling on my face, which she put at her feet!",
412943: "From the MZA forum (section - train reviews).\n\nCars in the standard BC livery....\nIn Penza a man got on, lay down on his berth above my seat.\nThey woke me, as is proper, 30 minutes before arrival. An hour later, when the train had moved a significant distance from Morshansk, and I was walking along the still-dark platform, I discovered I was wearing someone else's shoes. I'm in the boots of the upper Penza man. Forgive me, Penza man. Moreover, the shoes fit me perfectly, were very comfortable, I liked the style. My own old boots, which the Penza guy had to put on a few hours later, were too small for me, and anyway I was about to throw them out soon\nBut back to the train....",
412944: "ххх:\nWe had a tragicomedy at home, my son at 12, having watched all sorts of barvikhas and other crap series, decided to show off in front of his friends, dressed up like a supposedly cool rapper, baggy pants, cap sideways, and decided on camera to light a cigarette from the money he'd been saving for an x-box game, (he'd bummed the cigarette somewhere) in general at the crucial moment, when the filming is going, at the most needed moment when he's already ready to set the money on fire, I walk into the room, see all this, give a hefty clip round the ear, so the cap flew off, take away the thank-God-still-intact money, and the camera, with the words, \"So you don't get the idea I'll post it on youtube\" I leave the room, my son is crying and begging me not to do it for 40 minutes already. :)",
412945: "xxx: My mom is a master at focusing attention on completely unnecessary details of a story. \nxxx: If I tell her that on the way back from lunch I met a green mammoth surrounded by fascists and mounted police, mommy will definitely get interested in what I had for lunch.",
412946: "ХХХ: Collecting monthly reports on work done from staff.\nХХХ: 20 healthy men who came in one after another, each with the phrase \"Take my monthlies\" blew my brain forever...",
412947: "xxx: Found my full namesake on Facebook and wrote him that he has an idiotic last name and a stupid first name. waiting for a reaction.",
412948: "August 19. Husband's birthday.\nRiding in the car, listening to some radio. The announcer cheerfully broadcasts:\n- Few know, but today is a holiday - condensed milk's birthday! August 19, many years ago.. *well and blah-blah-blah further in the text*\nHusband, with an absolutely happy look:\n- Did you hear? I'm condensed milk!",
412949: "xxx: and do you think why programmers wear different socks? not because they're sloppy, but because they're of different types, and they all inherit from one class anyway :D",
412950: "<ri> going through someone else's site for work\n<ri> file /css/ie6.css\n<ri> first line: /* FUCKING IE6 - DIE! DIE! DIE! */",
412951: "xxx: In dial-up times I couldn't have dreamed in my worst nightmare that support services of various providers would call me offering high-speed internet one after another, and I'd tell them all to fuck off.",
412952: "From Habr:\n\nkarazyabko: How can you work where there's a roll of bubble wrap lying around?\nvvzvlad: very simple. there used to be two rolls.",
412953: "xxx: Sitting for the third day laying out a site, decided to take a break, went to the kitchen, on the floor a strip of light, first thought: \"Well this is totally not needed here\".... \nxxx: Went to drink beer",
412954: "ZoozooZ: The decision to start a new life usually ends with cleaning the apartment...",
412955: "A friend calls me and says:\n— My girl kissed everyone at the party. What should I do with her?\nMe:\n— Call her Brezhnev!",
412956: "ttt: this is fucking awesome. Sanya isn't going anywhere with us. Inga is a dumb bitch. I knew she was dumb, but this fucking dumb - that's something else.\nuuu: what happened?!!\nttt: shit happened. Inga filled out Sanya's form. this moron, when she couldn't reach him to find out his series/number, called her acquaintance Alexander and asked his.\nuuu: Didn't get it.\nttt: this idiot thinks that all people with the same names have the same passport data! now on paper Sanya is going with us, but only Sanya, Inga's acquaintance from Murmansk.",
412957: "Echuy: I can't work in these conditions!\nНастешка: What happened\nEchuy: The good-natured cook in the canteen gave me a double portion of roast for the price of one, now I've overeaten and can't sleep.",
412958: "ZeWine: today I saw the most tense person in the world.\nZeWine: A little old man in white pants and a white shirt in a shaking bus was eating currant sorbet.... with a wooden stick...\nZeWine: I've never seen such concentration...\nZeWine: only because of this he didn't notice his sneaking-up friend, who cruelly scared him with the shout \"Semyon!!! Long time no see!!!\"\nZeWine: feel sorry for the shirt(",
412959: "xxx: Today morning there was an epic fail. Dad leaves for work, half an hour later comes back, opens the gates, hurls the cat in, leaves",
412960: "Standing with a company in the evening, and a friend of mine got sweaty during the day and got white streaks on his black T-shirt. So a friend of his asks him:\n- And what's that on your T-shirt?\n- Salt from sweat.\n- And does it help??",
412961: "In a work project found a comment to a big crooked function saying:\n// This is Caesar's code. It simultaneously doesn't work and doesn't crash.",
412962: "Was handing in photos for printing. The girl accepting photos from digital media has a sign attached to her monitor (in big letters): \"Don't poke the monitor with your fingers!\"\nMe: are they really bugging you?\nShe: Yeah these glamour kitties with their extended nails are driving me crazy! Second monitor this month we're replacing!",
412963: "<Festral-Ranma> RJ45: btw do you know how to spot a spam bot?\n<RJ45> BY SPAMMY MESSAGES\n<Festral-Ranma> RJ45: well we have a spam bot on our intranet hub, and the admins can't catch it\n<Festral-Ranma> RJ45: it changes its IP and physical address as they say\n<RJ45> PHYSICAL ADDRESS\n<RJ45> RUNS AROUND THE APARTMENTS KILLS INTERNET USERS\n<RJ45> SPAMS FROM THEIR NICKS",
412964: "xxx: Fuck, the parrots learned to imitate the sounds of incoming contact messages. \nxxx: Fifth time I've run over, can't eat normally)",
412965: "WL-Cyanide: What if I've gotten fat and won't fit into my sarafan?\nNami-chan: You'll fit. It was hanging loose on you.\nWT-Cyanide: Damn. What if it's just right?",
412966: "xxx: What do you call a person who loves watching others suffer?\nyyy: A programmer",
412967: "xxx: what are you doing?\nyyy: lying, smoking, watching the sunset, wifi waves gently washing over my body...",
412968: "Breakmasher: I have a satanic elevator in my building\n­Breakmasher: The 13 button sticks on it\n­Breakmasher: Just rode to my native 17th floor. Halfway the number 13 lit up ))\nI had some fierce dark stuff playing in my headphones.\nBefore I realized the button was sticking - the brick had almost come out)",
412969: "xxx: today contact steals your time @ TOMORROW - RAPES PETS",
412970: "xxx: Better to get a bullet in the forehead than a fool for a wife.\nyyy: Ha. Try divorcing a bullet.",
412971: "Attitude toward a heroic feat strongly depends on where you are at that moment: in front of a tank with a bunch of grenades, in the tank, in the trench, or in front of the TV screen.",
412972: "Visited my uncle in the hospital - he drove drunk, wasn't wearing a seatbelt, broke his arm and six ribs. On the next bed - a guy, got into a drunken fight, dislocations, concussion, the third man in the ward got loaded at a shashlik party, decided to dive into the quarry - fractured spine.\nI leave the hospital, call mine: \"Sorry honey, but no more beer. I just now realized HOW alcohol harms health!\"",
412973: "From a review of Duke Nukem Forever:\n\n15 years of waiting down the drain!\n(the profile shows the author of the remark's birth date — 11.12.1996)",
412974: "Kiss Lovе: I'm on a diet too\nKiss Lovе: but felt sorry throwing the pelmeni away",
412975: "rushka: once I decided to go to a drunk party and, so they wouldn't... umm... fuck me there, I got an intimate haircut in the shape of a man's mustache.. You won't believe - it came in handy and worked!",
412976: "From a conversation with my nephew:\n\nMe: why don't you go anywhere in the evenings?\nHim: my parents don't give me money for parties :(\nMe: yeah, I remember - my mom didn't give me money for parties either. That's how Alyoshka appeared...",
412977: "Makas: tomorrow I'll get up early, have lunch and finish everything...",
412978: "Driving past a high-rise the day before yesterday. I see, on the 13th floor on the loggia hang five absolutely identical white T-shirts. Driving today and I see in the same place drying exactly the same 5 T-shirts, but red. First thought: \"The safe house is blown\" :)",
412979: "akul_a: how are you feeling?\narxont: That shouldn't concern you, you're not in the will",
412980: "Zloy Yogurt: Fed up. Bought a DSLR - PHOTOGRAPHER! Bought a DSLR and no money - wedding PHOTOGRAPHER!!! *WALL*",
412981: "she: Aaaaaaaaa! I just saw a cockroach on my laptop!!!\nme: It fell out of your head.",
412982: "шикич: my friends had a very exotic first sex by the way\nшикич: they were walking drunk at night home past the river, and there in the reeds stood a couch",
412983: "lizi: standing in a bookstore today:\n- miss, one book here seems to have gotten into programming from the sci-fi section by mistake\n- hmm, let me check. Who's the author you say?\n- Andy Oram and Greg Wilson.\n- Don't see it in the lists... What's it called?\n- Beautiful Code.",
412984: "XXX: My ex-husband was bragging the other day that he \"practices anal sex with his new girlfriend and is thrilled\"\n\nXXX: Today he sent an SMS that he's at the proctologist's appointment.\n\nXXX: Got me thinking...",
412985: "A serious reason to get up by the alarm clock appeared. Exactly at this time the girl in the building across the street does naked morning exercises in front of the window...",
}

outrecs = []
for r in recs:
    rid = r['id']
    assert rid in EN, f'missing translation for {rid}'
    en = EN[rid]
    ru = r['text']
    assert en, f'empty en for {rid}'
    assert en.count('\n') == ru.count('\n'), f'newline mismatch {rid}: ru={ru.count(chr(10))} en={en.count(chr(10))}'
    for tok in re.findall(r'<[^<>\n]*>', ru):
        assert tok in en, f'token {tok!r} missing in en for {rid}'
    outrecs.append({'id': rid, 'ru': ru, 'en': en})

assert len(outrecs) == 100
assert [o['id'] for o in outrecs] == [r['id'] for r in recs], 'order mismatch'

n = int(subprocess.run(['wc', '-l', OUT], capture_output=True, text=True).stdout.split()[0])
print('current out lines:', n)
assert n == 800, f'expected 800, got {n}'

with open(OUT, 'a', encoding='utf-8') as f:
    for o in outrecs:
        f.write(json.dumps(o, ensure_ascii=False) + '\n')
print('appended 100; batch 9 done')
