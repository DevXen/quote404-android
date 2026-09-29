# -*- coding: utf-8 -*-
"""Batch 10 for chunk_30: source-order records 901-1000 (IDs 412986-413086)."""
import json, re, subprocess

SRC = 'chunks/chunk_30.jsonl'
OUT = 'out/chunk_30.jsonl'

lines = open(SRC, encoding='utf-8').read().splitlines()
recs = [json.loads(l) for l in lines[900:]]
assert len(recs) == 100, len(recs)

EN = {
412986: "From a forum:\nLager: Remembered a story. 9 years ago, I worked in an audio-video store, also did video rental. Three \"tough guys\" come in and demand a Teletubbies cassette with a businesslike air. To the answer, there are 8 cassettes in one pack, they said they'd take all at once, paid and demanded to watch one. What happened next is hard to put into words, such a sweet childlike look on all of them))).\nAnd then one to the other: \"I told you, his name is Tinky-Winky!!! Give me 200 bucks!!\" They ran out of the store and I never saw them again. And we later sold the cassettes again)))",
412987: "xxx: Beeline went down, the TV didn't work, the internet died, I went and baked a cake out of grief. \nxxx: The household decided let it not work :)",
412988: "Default: I'm not arguing, you strongly influenced me and my worldview\nDefault: I started hating women more...",
412989: "xxx: Reading the news headline \"Anatoly Chubais will supply schoolchildren with tablets from ROSNANO\" and for a long time trying to figure out what tablet manufacturer POCHAKHO is",
412990: "From a forum:\nanubis345\nPromising and profitable work for everyone with 2-3 hours of free time to work on the internet from home. No investments. Ability to plan work independently. Free schedule. Details on the site: xxx\n\nplastikman\nand if my tits are small, will you take me?",
412991: "xxx: here's a client app in delphi. Something's off with the speed, easier to make a thread and entertain the user with a porn splash screen. He'll look and wisely declare that the app is advanced and has a friendly user interface. But when you have 200mhz of some God-knows-what server's frequency, 128mb of RAM and 30 seconds to execute a script, the script's life becomes like an episode from the movie \"Saw\".\n\nxxx: \"Hello, script, I want to play a game. For weeks you ran on a developer machine with 3gb of RAM and a core2duo 3ghz and didn't appreciate optimal algorithms. Today you'll get a chance to change. You have exactly 30 seconds before apache just kills you\" )))",
412992: "Shvitlanka:\nHere nobody cares who you were before: a marketer, a bank worker or a project manager. Here you're nobody. Dig. It's potatoes",
412993: "From habrahabr, from the discussion \"If PHP were British\"\n\nOleg Yozhic: It'd be a whole different story if php were Russian, I'm afraid it wouldn't be so polite...\n\nmayhem: code optimization would come down to inserting profanity before function calls — «анукаблядь str_replace()» just has to run faster than a regular str_replace()\n\nlicvidator: Or at least keep quiet about internal problems.",
412994: "News: Sony announced the release of two models of digital binoculars with photo-/video recording function.\n\nххх: Now we can watch the neighbor changing in replay.",
412995: "xxx: So what? Let's go, I'll help you pour the floor.\nyyy: Yeah right! Mish, this is the 3rd Saturday we come there and pour THE FUCKING BALLS! Stay home.",
412996: "xxx: sometimes you feel with him not like behind a stone wall, but like put against the wall.",
412997: "Dzhubba: I'm used to changing bed linen once a week. But my girlfriend is used to changing it twice a month. Hence conflicts, diaper rashes and other dermophytoses.\nHow to accustom a woman to cleanliness without getting into a fight?\n\nergust: Here's your plan for the next four months.\nBuy a couple of identical linen sets. For example, orange and green.\nAnd change by the scheme: orange, orange, green, green, orange, orange, green, green, orange, orange, green, green, orange, orange, green, green.\n\nAton_5: white–orange, orange, white–green, blue, white–blue, green, white–brown, brown. Oops. I think I'm writing in the wrong place…",
412998: "XXX: hi. how was work?\nYYY: hiiii! oh, good ) all day, like a bee\nYYY: ate sweets",
412999: "user1: and I'm more worried about the transport problem. when will we learn to move with a snap of fingers? so snap - and you're at the sea!\nuser2: snap - and in someone else's bed\nuser3: yep, with an \"I'm Feeling Lucky!\" button",
413000: "Discussion of news about disabled parking:\n\nAnd I'm generally happy for the disabled, look at the disabled spots at malls, only infinities and lexuses parked. They can afford it, and someone still badmouths \"shitty Russia\"!\nAnd it's also delightful how parking guards feel their social responsibility, a disabled person drives up, and they run up and carefully move the fence they put there so all sorts of irresponsible assholes don't park in disabled spots!",
413001: "News:\nThe King of Sweden Carl XVI Gustaf, traveling through Germany with his wife Silvia, had a curious incident. The crowned couple was not let into a German restaurant in Ladenburg. The establishment's owner didn't recognize them as royalty at all.\nIn the end the modest monarch Carl Gustaf and his wife, who is incidentally a German citizen, settled for pizza at a snack bar on the market square.\n\nComments:\nххх: at our place the restaurant owner would already be sitting with Khodorkovsky.",
413002: "xxx: I got myself a girlfriend. She's 90 60 90\nyyy: purple or something?",
413003: "FivE: Fuck, I almost shit myself just now\ndober: Why?\nFivE: You know, my cat, when I'm sitting in my terry robe, jumps on my back and clings with its claws when it's playing crazy?\nFivE: So here I am, night, silence, I'm in headphones killing zombies in the dark...........",
413004: "xxx: Ordered a wedding photo book (with 50% discount) from some guys. Prices at all such firms are the same, and with these I know the quality is good — in the future I'll order from them.\nyyy: How many times do you plan to get married?",
413005: "Topic: Engineers of «yo-AUTO» decided to use the Android operating system in the yo-mobile\n\nххх: my mobile won't flash from recovery.. what to do ?!?!?!\nyyy: hold gas + brake + high beam, and try starting the engine. When a green AnroYoMobile inscription appears on the display, immediately mash the horn, then you can plug in the flash drive with the image. I've already flashed mine. Good luck",
413006: "kitekatrix: The wedding was so fun that we forgot the certificate in the limo.\nThe next day my husband and the best man went to the garage of the firm where we ordered the car.\nA classic scene - TWO MEN walk in saying \"hello, yesterday we had a wedding...\"",
413007: "xxx: Yeah, 8 years ago I tried port wine and realized I'd never become a punk sleeping eternally in a puddle of my own vomit, with tears in my eyes I had to take off the leather jacket and go study to be a teacher at a children's music school in the flute class.",
413008: "Husband is playing WoT, stepped away from the computer briefly.\nMe: Honey, while you were in the kitchen, I was a bit naughty here...\nHusband: What did you do? (scared)\nMe: Just watched some porn.\nHusband: (relieved): Phew, I thought you SOLD THE TANK...",
413009: "Getor: You say:\"Bought a DSLR - photographer\". But half the population thinks: \"Bought a car - driver\"!",
413010: "dos: Reminds me of a story. 10 AM Sunday - doorbell rings. I open - two ladies like Jehovah's Witnesses.\n- Due to the misfortunes happening, don't you think God has abandoned us?\n- I don't believe in your God at all, and what misfortunes are happening?\n- Yes yes! We have a magazine here, you can take and read! blah blah blah.. Here's how to quit smoking.\n- I don't smoke\n- Here's how to quit drinking\n- I don't drink\n- Well here's how to raise self-esteem\n- Mine seems pretty good\nOne couldn't take it:\n- Then how to lower it...",
413011: "xxx: The goddess Ishtar — in Akkadian mythology — the goddess of fertility and carnal love, war and strife... associated with Friday.\nxxx: So many years passed, and people's Friday is still the same.",
413012: "ххх: when I'm sitting on the toilet doing my business, I'm most afraid of waking up",
413013: "My brother came from the village, selling furniture due to the move, told:\nYesterday buyers came for the soft corner, with movers, not very sober. Having led them into the house I warned them to be careful on the veranda, since the cellar lid (3.5 meters deep) was open, so as not to fall by carelessness. The buyer repeated to his: Heard? be careful here! Yeah - they said.\nThe deal happened, the movers carry out the couch, moving carefully, hallway, now the veranda. The cellar is open. But apparently the safety lesson was in vain. And like in a cartoon, the one walking backwards sharply goes down, quietly and almost noiselessly. Having climbed out, to everyone's surprise (they thought he was done for), absolutely intact, with a staggering gait, silently grabs the couch and carries on. Loaded up, left.\nAfter the buyer left, my son goes down into the cellar, and shouts from there:\nMom, Dad, there's 9 rubles in change scattered on the floor, should we return it to him?\nMom: No need, sonny, he paid for the ride",
413014: "About a suddenly spinning romance:\n\nxxx:\nAnd anyway, I wanted to clarify the terms of investment portfolios and deposit sizes with him then.\nBut it turned out I just sent a smiley.",
413015: "xxx: got a letter from ЦПО ГОУ ВПО «БГУЭП»\nxxx: meditating.",
413016: "Man is a very smart creature and is excellent at disguising himself as an idiot",
413017: "xxx: and as a kid I entertained myself by cutting a Möbius strip along the axial line, and then the resulting ones further too. and so on 8)\nyyy: your parents had no problems with your leisure. I envy them.",
413018: "Space: I remember, recently at night I caught a cab. Next to the driver sat (as I understood) his common-law wife, drinking beer and calling her friends with the text: \"What are you doing? And we're riding around\".\nAt the intersection she threw a pack of croutons out the window. I told her not to litter. She answered: \"Nobody asked you\".\nMe: \"I'll open the door now and run away with the money\".\nThen we rode in silence and kept clean.",
413019: "ххх: Sorry, don't want to talk right now, I have a difficult stage in life, you won't understand...\nууу: You think I've never had hard stages in life? Once there was even a time when I couldn't walk, couldn't talk, didn't understand what was happening around me...\nххх: Really? For real?\nууу: Yeah (( But then I turned one, and hope glimmered on the horizon )",
413020: "xxx: There's a training on conducting meetings.\nxxx: A colleague says \"All people have perception delays\".\nxxx: A girl interrupts him shouting \"I have no delay\"\nxxx: Colleague \"Congratulations\".",
413021: "ххх: getting old\nххх: today I lost a staring contest to my cat",
413022: "xxx: today I bought a watermelon at the supermarket. I come home - mom happily informs me she already bought a watermelon. dad just came home. guess what he bought..",
413023: "Дуроеб: so what, babe, coming to my place tonight?\nКиса: you know, I'd even agree, but something's bothering me here...",
413024: "ХХХ: Until I was 19 I thought tetanus was when it stands in the morning. I wondered why they vaccinate against it, it's fun!",
413025: "WerWolf: You know why I wouldn't want to date those who've had many girlfriends?\n\nWerWolf: As a programmer it's easier for me to write my own code than to dig through someone else's, written by a group of programmers who also had crooked hands.",
413026: "ххх: need to diversify the diet somehow: various salads, soups from nothing, porridge from an axe",
413027: "me: Let's go to the forest for mushrooms this weekend?\nshe: I can't right now, only after the wedding!!!\n\nThen I remembered her sister is getting married this weekend, but at first the stated requirement seriously threw me into a stupor.",
413028: "xxx: Sci-fi writers feared that people would become stupid and lazy because robots and computers would do all the work, and any information could be gotten from the net with a simple query.\nxxx: But they didn't account for people not being smart enough to use robots and computers, and too lazy to get information from the net with a simple query...",
413029: "111: why did you eat so much at grandma's if you're going to dinner with an admirer?\n222: grandma's not an admirer, you can't refuse her",
413030: "xxx: My brother's fiancée threw a scandal because he ordered cars with \"red rear headlights\" for the wedding motorcade. She doesn't want to hear any explanations.\nxxx: I think my brother is reconsidering.",
413031: "Juzzt: Once worked a night shift as a guard at a facility. So I'm doing my rounds, around half past midnight and I discover that employee ххх stayed late today and is arguing on the phone with his missus in raised tones, and they're barking at each other so that I can hear his wife yelling in his ear from the other end:\nххх: I'M AT WORK! YOU'RE DRIVING ME CRAZY!!! I'm not hanging around anywhere, I'm not walking with anyone, I'M WORKING!!!\nwife: I know how you work! you're never home for days!!! I know you're running around with women!!! At least admit it like a man!\nххх: oh I feel my beard starting to grow!!!\nwife: What beard??\nххх: A BLUE FUCKING ONE!!! A BLUE BEARD!!!\nwife: what, are you drunk again?\nххх: oh when I get home... we'll talk...",
413032: "Makzyr: you can't hide a watermelon in the fridge.",
413033: "uslashka: reading about Kazan \"I liked Kazan! The city is being built and growing beautifully. Even abandoned houses are abandoned neatly....\"",
413034: "xxx: At our work they give time off only for a wedding and a funeral.\nyyy: For your own wedding? Or someone else's too?\nxxx: Only your own.\nyyy: And for funerals also only your own? x)",
413035: "Vasily: we're doomed.\nVasily: on the fire shield by the entrance there's neither an axe nor a hook.\nVasily: I can't work in such an alarming environment.",
413036: "The phone rings at 2 AM, unknown number. I pick up, preparing curses:\n- Yeah?\n- Vasya has two apples, and Petya has a penguin. Which of the children can boast a beard at 9 years old?\nThe prepared long unprintable phrase flew out of my head somehow unnoticed....",
413037: "XBuddy: and my wife yesterday got a bunch of new clothes\nXBuddy: she washed my black windbreaker with her stuff :)))",
413038: "@naatalex: This retro oldschool of yours makes my jaws cramp.",
413039: "Марианна*: Can you imagine: my Rada won't eat adult dog food! She only eats puppy food((\n\n~Rozetta: well your family - the father doesn't allow being called grandpa, the sister decreases her age every year.... now the dog is rejuvenating too....",
413040: "xxx: I recently realized why people need mountains of porn. It's pre-reserved free space on the hard drive.",
413041: "Samuel: I'm so tired today, no robot-capacity...\nШурик: And you used to have them?\nSamuel: Have what?\nШурик: Well robot capabilities?",
413042: "Zuka: fuck, I can't even kill a fly with my diploma. It's completely useless",
413043: "Locki: In relationships it's always like that. A girl wants attention, respect, a first boy, a second girl. A guy wants a radio-controlled helicopter and a stormtrooper blaster. Misunderstanding.",
413044: "at work they installed new computers, the coolers are really noisy\n\nххх: in half a year you'll be freaked out by the noise! there was a case at the zoo, a fire alarm went off over the bear's cage, and he died an hour later!\nyyy: burned?",
413045: "From oper.ru:\n> Comrades, is it only me who gets chicks shagged in the top-left ad block?\n\nThere's contextual advertising from Yandex, it seems. Everyone is shown their own. I get plastic windows and books. Because I searched for them in Yandex. You get porn, unclear why, of course.",
413046: "yyy: I met a man, but I didn't like him, how do I politely decline further communication?\n\nххх: I usually asked to borrow money, a large sum, then they disappeared on their own... :)",
413047: "yellow5: I came up with a new TV show, Battle of IT Psychics. the best admins penetrate users' heads and guess what they clicked and why \"nothing works\"...",
413048: "XXX: What does your soldering iron have a three-position switch for? \"Off\", \"on\" and something else?\nYYY: No. It's \"Off\", \"Truth\" and \"Future Prediction\"",
413049: "ххх: in 40-50 years there won't be grannies on benches anymore\nууу: do you believe in the end of the world?\nххх: I believe in the internet and social networks",
413050: "xxx: What a country! \nxxx: It's almost September, and we still don't know who our next president will be!",
413051: "ххх: Question: when my sister and I both get married, what will our husbands be to each other?\nууу: Brothers. In misfortune.",
413052: "ххх and do her parents also doctors?\nУУУ yep\nххх both?\nууу nope\nххх mom?\nууу nah\nххх dad?\nууу nah\nххх who the fuck?",
413053: "Doorbell in the morning on a day off.\nConversation through the door.\nMe (sleepy and annoyed): Who's there?\nThey (in a singsong humble voice): Jehovah's Witnesses.\nMe (on autopilot): And what does your lord Jehovah say about headless mutants?\nThey (with a groan of despair): What is this, did you all conspire?!\nSound of elevator doors closing.",
413054: "xxx : fog in the morning is insane, can't see shit\nyyy : maintenance on the server, minimal draw distance X)",
413055: "ЧайнеГг: if there's no water in the tap\nМорфина: then somewhere there's two of it )",
413056: "from a dating site\n\nСерёга: hi! let's test how nimble my tongue is?\nНаташа: let's! say quickly three times: \"The exhibitionist's biceps aren't big\".",
413058: "ash: well tell me in two words how you rested\nses: fuck ing great",
413059: "Kot_kotych: my photos at work caused a furor (I showed them on my tablet) at least one person wanted to go to Karelia, another one a camera and two a tablet *ROFL*",
413060: "tverse: A wall next to the mall, completely wrapped in BUBBLE WRAP. For doubters it says right on it \"get rid of stress\".\nfunny_undead: The phrase \"go kill yourself against the wall\" unexpectedly took on new colors!",
413061: "чве: after lunch I accidentally noticed I'm in jeans and socks, though usually at home I wear only sweatpants. And since then I've been walking around thinking: where was I going to go, fuck!!!???",
413062: "@bingestreift They brought a laptop to look at, they say it's the fourth external network card this month that died. What a sociopath...",
413063: "xxx: Oh, when I watch these monsters in movies I don't sleep at night, I'm scared. All the zombies climb in my window. And you?\nyyy: no. I have a mosquito net.",
413064: "xxx: Hi! Do you believe in the end of the world?\nyyy: Hi. First, how did you know my name is Sveta. Second, what end? What are you talking about?",
413065: "metallcat: waking up feeling like I got enough sleep. first thought - I overslept =)",
413066: "xxx: where did you disappear to?\nyyy: you won't believe, went with the builders on a UAZ to inspect sites\nyyy: but now I know why this UAZ is called a \"loaf\"...\nyyy: and believe me, it has nothing to do with bread :-/",
413067: "xxx: today something incomprehensible happened\nyyy: what happened?\nxxx: I still don't understand how it is\nxxx: I sewed a Möbius skirt",
413068: "xxx: I heard that road cameras can't register a car going faster than 220-250 km/h\nууу: Oh, so I can accelerate to 300 on the highway and spit on speed limits?\nххх: Yeah, you just need to contact the dispatcher and report that takeoff went fine and you're switching to climb mode.",
413069: "xxx: What's up?\nyyy: Fine. Sitting in the Bahamas.\nxxx: When did you get rich, you bum?\nyyy: Nah, all good. They brought a batch of tile grout. Color \"Bahamas\". Was unloading. Got tired. Sitting.",
413070: "Mom is editing a screenshot picture from Word in Paint, finished, copied, shouts: \"It won't close!\". How won't it close, I say. \"Just like that, I press the cross, and it won't close\".\nI'm figuring out what could be wrong, and then it dawns on me. Remember in the cartoon \"Treasure Island\" squire Trelawney tried to smash a landscape painting with a rifle butt, mistaking it for a window?",
413071: "xxx: hope my neighborhood didn't scare you?)\n\nyyy: The neighborhood didn't scare me. When I was young and handsome, I had a girl living in this neighborhood. In general the neighborhood is familiar. Then she married my friend. And 15 years later he filed a claim against me that I ruined his life.",
413072: "yyy:\nIn general, X\nI understood a simple thing!\nYou're my most faithful and reliable love!\nBecause for years now you've been keeping your promises to me\nYou didn't promise shit - and you don't do shit. A rock of a person, I couldn't do that",
413073: "xxx: hi, here?\nyyy: yeah\nxxx: I'll tell you a secret now >_>\nyyy: <_<\nyyy: all clear, tell it",
413074: "From a forum:\nVilfred_Sagen: The Chinese had a case, they had a pianist Liu Shi Kun, so he came to the USSR and took first place at the P.I. Tchaikovsky competition, came home with the prize and the Chinese chopped off his fingers for the European prize in 1940 or so, even before the PRC it seems, what traditions\n\nsiliconec: Better if there turned up a certificate for the Chinese pianist who took first place in 1940 at the P.I. Tchaikovsky competition, held since 1958\n\npianist: And it doesn't matter. Not in 1940, but in 1904. Not Chinese, but Japanese. Not a pianist, but a masturbator. But that doesn't stop screaming about human rights, right?\n\nDF: and not fingers",
413075: "ххх: eating potatoes with mushrooms\nххх: potatoes are my own, onions my own, picked the mushrooms with my own hands - everything's my own!\nУУУ: and the sunflower oil is yours? )\nххх: well yeah I squeezed it myself\nххх: from the neighbors ;)",
413076: "Zephyr: they said here that the epiphysis lets a person read minds at a distance, but it shrinks with age.\nMermaid: our brain's capabilities are limitless, imho\nZephyr: yeah. that's why military doctors are interested in the epiphysis. Because if you develop it, the military could read minds at a distance and wouldn't need comms.\nZephyr: imagine how many psychics there'd be\nMermaid: I imagine how many people offended at each other there'd be :D",
413077: "xxx: So domestic satellites stop falling, deputies are going to adopt a number of important amendments to the law of universal gravitation.",
413078: "Соник: riding bus 32 to the draft office, who knows which stop to get off at and where to go?\nshtakeTT: when you see sad young people with a dejected look, follow them",
413079: "n_n: but we're different, and opposites attract, right?\nApril: idk. are you attracted?\nn_n: to the earth mostly\nn_n: but me and the earth are different, hard to argue",
413080: "xxx: in our toilet next to the empty toilet paper roll, cynically lies a roll of paper tape. Who's so kind?",
413081: "Wife: Hello, my dearest half!\nHusband: am I a dearest creature, or do I have the right?",
413082: "fabrichnova: And if you hang a person by their legs, upside down. Will they be able to drink and eat in that position?\ngulnara_2009: Mussolini couldn't, he died\nriazhenka: Mussolini died a little earlier than he couldn't, because first he was thoroughly shot.\n(c) useless_faq",
413083: "xxx:\nwas walking with the kid near the house recently. we see - a big repair truck pulled up, dirty. my son got excited (he's only 1.5) started admiring, OGOGOGO!! O DIDIDA!!(that's wow!). and three repairmen standing nearby, smoking. stern men in work clothes. one looked intently and says: IF YOU DON'T LISTEN TO YOUR MOM, YOU'LL DRIVE SUCH A TRUCK TOO!",
413084: "doz10us: One AM. Outside, police are picking up a girl without getting out of the car and talking to her across the whole street through the loudhailer.",
413085: "- Screw it! Yesterday he came, put his hand on my head and says \"Guess what's on your head?\".\n- ???\n- \"A brain-eater\", he says, \"And guess what it's doing there?\"\n- !??\n- \"Starving!\"",
413086: "Антон1998: Hi, let's get acquainted! My name is Anton, I'm 13 years old. I love big titties! Very big titties! Do you have big titties?\nЭдельвейс: You poor thing, did your mom forget to feed you?",
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
assert n == 900, f'expected 900, got {n}'

with open(OUT, 'a', encoding='utf-8') as f:
    for o in outrecs:
        f.write(json.dumps(o, ensure_ascii=False) + '\n')
print('appended 100; batch 10 done')
