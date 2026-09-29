# -*- coding: utf-8 -*-
"""Batch 8 for chunk_30: source-order records 701-800 (IDs 412786-412885)."""
import json, re, sys

SRC = 'chunks/chunk_30.jsonl'
OUT = 'out/chunk_30.jsonl'

lines = open(SRC, encoding='utf-8').read().splitlines()
recs = [json.loads(l) for l in lines[700:800]]
assert len(recs) == 100, len(recs)

EN = {
412786: "from ICQ\n\nххх: my phone automatically replaces words\nххх: it replaced lexus with tram, and the word \"winter\" with ass",
412787: "Valiant@dirty.ru:\n2010:\n\nNo emission of harmful substances from asphalt occurs even in the abnormally hot weather that set in Moscow in mid-June, GPU \"Mosekomonitoring\" director Evgenia Semutnikova told journalists on Thursday.\n\n2011:\n\n\"The main differences between tiles and asphalt are related to the composition of materials. At temperatures above 25 degrees asphalt contributes to emissions into the ambient air. A number of hazardous substances are present in asphalt emissions at such temperatures,\" noted E. Semutnikova.",
412788: "xxx: saw a new ad where Activia yogurt helps digestion with shashlik. I'm wondering when to eat the Activia. Before or after the shashlik?\nyyy: instead",
412789: "heavenmaster:\n\nAt the new apartment we found books on C++, STL and UNIX, a kama sutra with new illustrations and a dozen books on Christianity.\n\nThey coded, and when it didn't work out, they fucked and prayed.",
412790: "yyy: how do I force myself not to want to buy another bag?\nxxx: I think there should be only two bags: a dark one and a light one.\nxxx: in the dark one you carry the severed heads of your enemies\nxxx: and lipstick in the light one.",
412791: "xxx: I'm in the forest, and the internet here is really shitty. and five years ago to this complaint they'd have said with round eyes: \"are you fucking kidding?!\"",
412792: "Comment on the news: GTA blamed for the London riots\n\nххх: and Tetris is to blame for expensive housing prices, because half the building materials disappear somewhere.",
412793: "ххх: my computer is so cool now that I don't even need pornography anymore\nххх: I can fap to the system specs",
412794: "[Slikit]: tried to fall asleep to the sounds of an excavator working right under my window or something similar\n[vearjana]: At work I learned to fall asleep to the sounds of an angle grinder, with my eyes open and periodically clicking the mouse",
412795: "Read an article on Reuters about the reusable office paper \"i2R e-Paper\" invented by the Taiwanese (thermal printing principle, when voltage is applied to the contacts the text disappears, up to 260 times).\nFirst thought - just don't let them get to toilet paper with their experiments!",
412796: "from LiveJournal:\nquestion: I want to get a hormonal IUD (presumably Mirena), but it says it's effective for up to 5 years, and I'll definitely take it out in two-three years. Could it happen that the Mirena tries to \"use up\" its whole resource and starts releasing twice as much hormone daily as it should?\nanswer: it definitely will, as soon as it learns of your insidious plans.",
412797: "A friend is building himself a new computer for a pretty round sum and the question arose whether to change the case too.\n\nНиколай: on one hand it's not needed at all, just replace the coolers on yours and it's fine\n\nСергей: and on the other hand, you'll need to show where you spent such a big sum of money. And if you point at your old system unit, people won't understand you :)\n\nНиколай: what people? You and Egor? =)\n\nСергей: no. usually it's relatives.\n\nСергей: Like they say, for example, \"fly to the sea with yours, rest like a human\", and you're like \"I have no money\". They: \"And where do you spend it?\" And then you show them your old system unit...\n\nСергей: AND THEY DON'T UNDERSTAND YOU",
412798: "xxx: You see Yul, to help you with your computer first I need to at least roughly figure out how old it is.\nyyy: Well it's very old!\nxxx: Well how old roughly? 3? 5?\nyyy: Well it's so old that it's even still white...",
412799: "yyy: I'm going to wash up now and then sleep\nxxx: and I'm going to play now, and then play",
412800: "Playing AION with the boss:\nxxxx: Dim, lend me some money, can't afford the chainmail\nyyyy: Against your salary, or what?",
412801: "xxx: Hi everyone! please tell me what to do! Here are the cities I want to visit:\n1.Amsterdam\n2.Belgium\n3.France\n4.Germany\n5.Italy\nIs it realistic in 2 weeks?\nyyy: I suggest starting with a geography textbook. Amsterdam is indeed a city, all the rest are entire countries. You'll be surprised when you see how many cities there are.",
412802: "Апачай Хопачай: progress, damn\nАпачай Хопачай: mom is lying on the armchair a meter away from me with her laptop, calling on Skype and sticking out her tongue...",
412803: "They dumped the computer hardware inventory on us admins, there's no one else.\nAccounting gave a printout from the database of what's listed.\nBut our accountants are advanced: when a batch of hardware arrived - they entered the first one manually, and the rest by copy-paste. I look at the printout: I need to find thirty monitors of one brand with one serial number on the premises. Yeopta.",
412804: "xxx: seems like I cleaned up, good job, but damn, I'm sure when mom comes she'll find something else to complain about(\nxxx: like \"why did you throw out the flippers? your grandpa bought them!\"\nxxx: although in the last 15 years they were used, as far as I know, once\nxxx: when my friend decided to go get beer in them...",
412805: "qwerty_qwerty: every Friday I'm in random.",
412806: "xxx: Did you know that Petrovich, well, the homeless guy who lives by our entrance, he's vanilla!\nxxx: Today he said he loves rain.\nxxx: You can wash in it.",
412807: "She: Tonight we have a romantic evening. There'll be a tasty dinner: lamb with rice in sauce, salad and all of it with candles.\nHe: Can I pass on the candles?",
412808: "xxx: people trying to sneak into _support_ with the \"I just want to ask\" approach give me a certain thrill.",
412809: "ClrV: you know how the internet \"back then\" differed from the internet \"now\"? Now, clicking a link and missing, you say: \"Oh fuck! Where am I?\", and back then you'd go drink tea, come back, see that you clicked the wrong link, and the request was still being processed, and you'd calmly click the right one and go drink more tea...",
412810: "inferno: there's webkit inside, everything workses great\nasquante: did the flimsy scripts works, did ajax start to whistle?",
412811: "Рилос Дельмс: If the pelmeni floated up, are they done?\nРилос Дельмс: Or did they die?",
412812: "me: why can't I split into two mes? ) one would go for the insurance, the other for shoes. one - to the registry office, the other to work\nDIRK: you can! you can clone yourself )\nDIRK: you'll all sit together and start whining, not one will go anywhere x)",
412813: "xxx: still, it doesn't sound great: bridal bouquet\nxxx: speaking as a venereologist",
412814: "Мэд: what's your nick?\nAsh: Фыр Тушпу\nAsh: Ash Neige\nМэд: Фыр Тушпу sounds way more interesting\nМэд: so annoyed",
412815: "Ghalghay_Khant: When I iron trousers, I thank God that people have only two legs.",
412816: "xxx: the US Fed's goal revealed - soon the US national debt will overflow uint64 and automatically zero out on all computers in the world, nobody will owe anybody anything",
412817: "XXX: What should I write in the \"Confirm password\" field?\nYYY: Write \"I confirm\".",
412818: "He: You've lost so much weight!\nShe: Is that a compliment? :)\nHe: It's horror!",
412819: "xxx: by the way, did you see Dima?\nyyy: Yeah, I did )\nyyy: We live in the same city :)\nxxx: that doesn't stop me from not meeting people for years :D",
412820: "[17:59:03] Mikki3154: one sec, I'll just eat quickly, 'cause after six I can't",
412821: "Лайк \u200e(9:52):\nLet me explain simply: arguing with grannies in a queue is like shitting on the ceiling. You'll be all covered in shit, and the ceiling stays clean",
412822: "yyy: Honey you're nervous today, let me read you a poem, you'll calm down.\nххх: I'm not nervous, and what's the point of me seeing your poem in chat?\nууу: Night, street, lamp, pharmacy...\nххх: DICK, PENIS, COCK AND MORTGAGE\nххх: Told you - everything's fine",
412823: "xxx: most pedestrians cross by the rules \"green — walk, red — run\".",
412824: "xxx: In the news constantly: Medvedev here, Medvedev there...,\nxxx: naturally the headline: \"Medvedev attacked a group of tourists in Kamchatka, two people died\" was at first read as, well you know",
412825: "xxx: If you're trying to close a square container with a round lid – it's Monday. If it works – it's Friday",
412826: "Santyaga: A real pillow should be comfortable for sleeping, for reading, for watching TV. It should adapt to its master.\n\nSantyaga: A blanket is the right pillow!",
412827: "xxx: Programmer job opening. Item three:\n3.Knowledge of natural languages: written Russian — without grammatical errors; spoken Russian — sufficient to clearly formulate thoughts with a minimum of filler words and profanity.\n\nxxx: Makes you want to wish them luck.",
412828: "xxx: Turns out only six \"letters\" in human DNA are responsible for eye color\nyyy: Yep, RRGGBB",
412829: "xxx: Went to the garbage chute to throw out a watermelon. A whole one. The watermelon, fuck, doesn't fit. Scratched my head, went and got a knife, started cutting it to make it fit. A neighbor comes out. Looks. What are you doing, he says. And I realize I'm slicing the watermelon right in the chute hopper. \"Having dinner\", I say. Silence.",
412830: "ххх: I want to be a crime photogropher, photograph crime scenes, corpses :-D\nууу: let me guess why\nууу: because they don't twitch, don't blink and there's no motion blur?\nууу: And they don't fuck your brain\nууу: good clients, in general",
412831: "ХХХ: How do you know, maybe he's my betrothed?!!\nУУУ: He's your be-widened, fuck!",
412832: "Vera: google is basically a member of our family\n\nVera: look, before everyone went to the eldest in the family for advice, and now - to google\ngoogle is the wisest\nand that's great, because google won't ask you why you need a cottage cheese casserole recipe at 9 PM, what, you still haven't made dinner for the kid?\n\nLyuda: and it also won't ask why look at an expensive car, you have no money anyway\n\nVera: exactly!",
412833: "offreal: We get into a friend's building very simply - in apartment 25 lives a granny who thinks the intercom handset is like a walkie-talkie - to talk, you need to press the button",
412834: "From LiveJournal:\n\ntehhi_sheck: how do you picture it? kick him out of the house with the words \"don't come back until you find a job?\" he understands everything in words and wants to find something himself, but in reality - seems to lack motivation =((\n\nliannotel: There are actually a fuckload of options :)))\n1) Humane. Sit next to him, turn off the ruler, open a job site. Pick a vacancy, send a resume, in 2 days give him the phone to call back and clarify.\n2) Sadistic. Give him mind-blowing sex, allowing absolutely everything - so he remembers for life. And after sex stretch sweetly and say - that was the last sex with an unemployed man in my life.",
412835: "Boss: Why do you go to the Client so unshaven? Aren't you ashamed?\nMe: Well our situation with the project is such that they don't look at our faces anyway. As soon as we walk in - they immediately turn us around, pull our pants down and start the meeting.",
412836: "xxx: Barely opened my eyes today after yesterday. Got to the kitchen on autopilot, reach into the cupboard for cereal, and suddenly in huge letters: BRAN, FUCK!",
412837: "xxx: To boost Skolkovo's credibility it urgently needs to be renamed to NII CHAVO. Then people will believe in it.",
412838: "xxx: and please solve the problem with transporting chicken manure to another plot\nyyy: shit question!\nyyy: oops, I mean, will do ;)",
412839: "About the movie \"Johnny D\":\nThe plot tells of the legendary bank robber John Dillinger. The main role was played by Johnny Depp. Interestingly, the movie's title applies both to the hero and the actor who played him.\nComments:\nMiss Murple as Miss Burple.",
412840: "usernickname: so you got a girlfriend?\nKenny: yeah\nusernickname: and who is she? )\nKenny: a girl",
412841: "From LiveJournal (tat_oshka)\n\nTwo grannies are sitting on a bench. Definitely around 70.\nOn the bench between them cards are laid out\n- love awaits you\n- huh?\n- LOVE AWAITS YOU!!",
412842: "maxwell: I don't even have a ringtone set for you on my phone!",
412843: "from the discussion of a system junk cleaner for windows:\n\nxxx: great prog! cleans windows in one go. then you can format the disk and install linux!",
412844: "30 seconds after sex the phone rings.\nguy: remove the children, there'll be swearing now!\ngirl (wiping the sheet with a towel): I removed the children, you can swear.",
412845: "ххх: so, did our comrade get hired as your designer?\nууу: No.\nххх: why?\nууу: Well, he came to us, walked around, saw our corporate wallpapers on the desktops, with the logo, and asks:\nууу: Who drew this?\nууу: We answer: so-and-so, our employee.\nххх: and?\nууу: Turned out it was his work from his portfolio, just with a logo drawn on. He spat and left.",
412846: "[Arturkkas] YOU'RE ONLY SO TOUGH ON THE STREET, COME MEET ME ON THE INTERNET AND I'LL SEE WHAT KIND OF COCK YOU ARE",
412847: "xxx: Was at grandma's, logged into Skype, out of habit didn't log out of my profile\nххх: And what do you think, did granny just log out of my profile and enter her login and password?!\nххх: No, she went and changed the account name from mine to hers!!! And the photooo!\nххх: My friends were quite surprised...(",
412848: "xxx: I wanna go to the sea, drop me the address.\nyyy: City of Adler, Sea",
412849: "XXX: once I was parking in winter - my wife was standing there, giving unsolicited advice. \"turn the steering wheel forwaaard\" - she shouts.\nXXX: now when it comes to letting her drive, I say \"as soon as you learn to turn the steering wheel forward\".",
412850: "Starink: hey! How are you? How's summer?\nKOrsar45: Great summer. They're taking the cast off tomorrow, by the way :)",
412851: "<xxx> that's the beauty of ntv\n<xxx> if you turn it on and see good acting\n<xxx> it's the news",
412852: "From a blog:\n\nI always carefully watch what my child watches, TV has become such crap. I bought useful discs, about animals, about experiments, about how planets work, about space, etc. I turn them on calmly, knowing they won't show my treasure anything extra. Today I put on \"To Kids About Little Animals\"...\nNow I'm thinking how to wean the child off the phrase: \"I'm not a baby, I'm a male!\"",
412853: "Алина: fuck, someone washed my keyboard!\nАлина: it has some letters on it!",
412854: "MTS philosophy: what came first - the egg or the deer?",
412855: "Yesterday was a party. Today I barely got up and standing in front of the bathroom mirror thought \"how shitty I look at 20\". Upset, went to the kitchen to drink tea. My passport's on the fridge. I took it... and I'm standing in a stupor. turns out I turned 32 today",
412856: "ууу: if it's not too much trouble, could you come over tomorrow and install photoshop?\nххх: depends on two things\nххх: 1) at what time? 2) who'll be home?\nууу: 1) whenever's convenient for you =) 2) I'm alone from 8 to 6 =)\nххх: hmm..\nххх: no one will be there from 8 to 18 for sure?\nххх: I don't need witnesses. the photoshop is pirated!",
412857: "xxx: All I could do was watch their swollen bodies, in places you could see the insides begging to get out, it was disgusting to watch, but I understood... the pelmeni were done.",
412858: "okumo: I love condensed milk\nokumo: eating condensed milk!\n...\nokumo: everything's in condensed milk\n...\nokumo: I hate condensed milk (((",
412859: "From a forum:\nxxx: Windows 8 will support speech recognition\nyyy: maybe better not? it'll learn to answer back, argue, you accidentally swear - it'll get offended, won't boot for a week...",
412860: "From a forum:\naaa: ...learned html overnight...\nccc: I wish! You don't live beyond the polar circle, do you?",
412861: "xxx: I just experienced cognitive dissonance. the label peeled off a tube of super glue.",
412862: "accountant: the printer doesn't want to print for some reason, and today's the advance payment day.",
412863: "xxx: today I fell on the dog\nxxx: was tossing on the couch\nxxx: and fell off it\nxxx: fell softly\nyyy: how is she feeling?\nxxx: the dog's fine\nxxx: looks at me a bit strangely\nxxx: and when she barks, she stutters\nxxx: and now she barks constantly",
412864: "zzz: porn movies in 1080p are something else!\nzzz: yesterday I spotted a cavity on one actress )",
412865: "On a social network, a photo of a girl behind the wheel of a bmw. Photo caption: 218 horsies :0\nFirst comment: 219 :D",
412866: "I work at a local internet provider, got a letter:\nDue to the internet outage from August 13 to 14, and considering it happened on days off, when my beloved instead of sitting on social networks and harvesting her virtual farm spent almost two days nagging my brains out with inexhaustible energy, I ask you to credit back to my account the unjustly withheld funds for two days and at your discretion a bit more as compensation for moral damages!\nRespectfully, XXX",
412867: "xxx: judging by the amount of pornography on the internet, after the XX century came not the XXI, but the XXX century",
412868: "xxx: I already saved our company a bunch of money.\nyyy: you mean your low salary?",
412869: "-=[ADM]=-: our whole office was stripped of bonuses\nyellow5: in connection with Builder's Day?\nyellow5: like, quit building yourself up here...",
412870: "ххх: marina is fuming\nууу: pms?\nххх: yep\nххх: the plan\nххх: for the month\nххх: is falling through",
412871: "Shamuel-27: Our mechanic was quitting, wrote a farewell letter about how wonderful it was for him here and with what pain in his ass he's leaving. However, there was reason to suspect his insincerity, since the first letters of the sentences spelled: \"Fuck this job\".",
412872: "During maintenance on a pirate WoW server, the admin's post:\n\nThe shop is off. Completely off. And even if you miraculously got in there, don't buy anything there. Don't buy anything at all. And don't even change your faction. And don't change your name either. And don't change your appearance either. Don't touch the personal account at all until there's a forum message that the core is stabilized and you can make purchases in the personal account. And until then don't touch the shop. Don't touch it at all. If another thread appears about how you spat on all the warnings and ordered something in your personal account and it rolled back, the thread will be corrected according to its content, i.e. \"I'm an idiot\". Repeating once more - Don't touch the personal account.",
412873: "xxx: Plankton, vegetable, tree.\nxxx: The evolution of an office worker.",
412874: "Having worked a couple years as a web programmer, I'm writing a C term paper.\n\nLike a dog after every line I hit save, with a light press of Alt + Tab I switch to the browser and refresh the page.\n\nPavlov would cry with joy.",
412875: "ааа: About blood and dogs my most epic memory is of my daughter's tutor's dog. It was a labrador teenager, golden, large, with a long (undocked) tail. He was very friendly, very happy about everyone and from happiness whacked his tail on whatever, including walls, the fence, door frames... as a result he smashed his tail bloody, but that didn't stop him. His tail was regularly bandaged, but from such powerful wagging all the bandages flew off, he'd bang his tail on something hard again, blood flowed again and fanned out across the room\nааа: They'd catch him, bandage him again, but it lasted 15-20 minutes.\nааа: In short, the owner removed all the carpets, covered the furniture with old sheets - a horrifying sight. Everything in bloody stains.\nббб: Mda...\n- And why is everything covered in blood at your place?\n- Oh, our dog is just very happy...",
412876: "xxx: Is celery tasty?\nyyy: Once our mom decided to put the whole family on a diet, and cooked celery soup, the diet turned out great, I didn't eat anything at all :)",
412877: "After another sock \"eaten\" by the washing machine, its wash stage called \"spin cycle\" acquired a new meaning for me...",
412878: "xxx: Dad suddenly found out a month ago that I'm already 18. He's fucking tired of going for beer himself.",
412879: "xxx: yesterday the internet was down\nxxx: I had no idea I had so many games on my computer",
412880: "ххх: yeah laptops these days\nххх: in the near future something like this will be totally normal\nххх: - vovochka, why didn't you do your homework?\nххх: - maryivanna, my laptop tore!",
412881: "xxx: learned at the store that in veal-flavored doshirak the meat is more tender than in beef-flavored doshirak",
412882: "xxx: I think the modern world needs a new superhero.\nxxx: He'll hunt down the voiceless karaoke singers in cafes and apartments\nxxx: AND SHOVE MICROPHONES UP THEIR ASSES WITHOUT VASELINE FUCK!",
412883: "xxx: give me something beautiful, useful and wearable?\nyyy: I'll give you a tinfoil hat. beautiful, wearable and protects from alien invasion",
412884: "A manager's call to a potential client, I'm sitting nearby, I only hear the questions:\n-Hello.Do you deal in trade equipment?\n-...\n-Condoms?\n-...\n-You said \"personal safety products\"\n-...\n-So you sell weapons?\n-...\n-Ah, workwear...",
412885: "ххх: Read somewhere that a person on average eats 8 spiders in their sleep over their lifetime. With the order in my apartment that's a daily norm (",
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

# check current output state BEFORE append
import subprocess
n = int(subprocess.run(['wc', '-l', OUT], capture_output=True, text=True).stdout.split()[0])
print('current out lines:', n)
assert n == 700, f'expected 700, got {n}'

with open(OUT, 'a', encoding='utf-8') as f:
    for o in outrecs:
        f.write(json.dumps(o, ensure_ascii=False) + '\n')
print('appended 100; batch 8 done')
