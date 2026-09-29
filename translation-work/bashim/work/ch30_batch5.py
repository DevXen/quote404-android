#!/usr/bin/env python3
"""Append chunk_30 records 401-500 (ids 412482-412581)."""
import json, os, re

BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC  = os.path.join(BASE, "chunks", "chunk_30.jsonl")
OUT  = os.path.join(BASE, "out", "chunk_30.jsonl")

EN = {
412482: 'xxx: Ded put the cartoon "The Turnip" up for distribution. People are pulling and pulling -- can\'t pull it out.',
412483: 'XXX: my beloved isn\'t writing to me today :(\nYYY: beloved is writing an sql query\nYYY: want beloved to write you an sql query? ;)',
412484: 'xxx: walking home. carefully. then a total hurricane started. I\'m going from bus stop to bus stop, under awnings. almost no rain now. stomping along almost dry, 5 meters to my entrance\n\nxxx: AND THEN SOME FAGGOT DECIDED THAT DRIVING THROUGH PUDDLES IS COOL FUCK!!!!',
412485: 'xxx: Thought today about what the gameplay in Civilization would be like if the authors aimed for total realism.\nxxx: Turn 1: a city is built.\nxxx: Turn 2: oil deposits found.\nxxx: Turn 3: the state is occupied by NATO troops.\nxxx: Turn 4: you are executed by hanging.',
412486: 'Los: who can tell me how websites are made?\nNadufka: slowly\nNadufka: and usually badly',
412487: 'I work as an HR manager. Didn\'t think finding a cleaning lady would be so fun. Hard club music instead of ringtones from an elderly woman, email "pusi-pusi@...". A cleaning lady who finished beginner programmer courses. A cleaning lady who lists herself as a design engineer. But my favorite: "Desired schedule: remote work".',
412488: '<Георгий Лобушкин> In late July 2011 people SUDDENLY discovered that search engines index pages on the internet!',
412489: 'xxx: where\'d you disappear after the trip? by the way, how was it?\nууу: everything Thai becomes clear...\nууу: I was getting treated',
412490: 'holovatka> I have different crushes. When one of them visits me, I start waking up early around 5 and lying until the alarm, my appetite is completely gone, sometimes for a couple days I can\'t remember what I ate.\n\ngadfather> How romantic. And what about the chair?',
412491: 'ЦЦЦ: Andryukh - I need a working machine with no lags and no file-hammering. In half a year I tested system units (not counting the video card) averaging 40k, on two OSes - XP (32bit) and 7 (full). I already have an army of broken keyboards and mice from crashing program settings, critical errors and other bullshit. I\'m sick of going to a girl\'s place to record music on a Mac - she doesn\'t take money, hints at sex, but that would be fraught. And she hints that she\'s sick of me coming over =)\nAnd on a Mac - you turn it on, install and work\nЦЦЦ: OS licensed, programs licensed\nЫЫЫ: wellllll. then you don\'t need a Mac, you need a rail\nЦЦЦ: what\'s a rail?\nЦЦЦ: a server or something?\nЫЫЫ: a rail is an iron thingy. explaining how to use it. 1) put your hands on the rail 2) smash them until they\'re straight (use this same rail as the straightness reference) 2) Profit! Programs don\'t glitch, Windows doesn\'t crash',
412492: 'ххх:\nif you read this comment, you must add the same comment to 3 videos, otherwise you\'ll die\n\nххх:\nI fell for it myself!\n\nууу:\ndid you die?',
412493: 'xxx: Saw a crocodile in the terrarium. You could tell it from a dummy only by the subtle twitching of its tongue. The sign read: "Alive, fed, resting."\nxxx: The other day I ran around, crawled home, ate my fill and collapsed on the couch. Not reacting to external stimuli.\nxxx: Wife\'s comment: "Alive, fed, resting."',
412494: 'From the discussion of photos of a Norwegian prison (by the way, 12 sq.m, furniture, TV, shower, etc.):\n\nxxx: Indeed, the prison regime in a country is one of the most important factors to take into account when planning your vacation.',
412495: 'A friend is calling right now:\n— My wife\'s phone is a "google", it\'s on Linux, right?\n— If you mean Android — then yes.\n— Yeah, that\'s the one! Then this question is definitely for you. She dropped it in salt water at the sea and now it won\'t turn on, what should I do?',
412496: 'Xiao: Ball lightning scattered a herd of 84 sheep in Tuva (c) IF-Siberia\nBlind: they must\'ve started testing Diablo 3',
412497: 'ххх: No, Ira, the action genre is not for you. I watched you play Alice, and here\'s my verdict: you\'d get killed even in the "Klondike" solitaire.',
412498: 'xxx: Tell me, Fedya, as a successful businessman, is it worth hoping for fruitful cooperation with a company whose list of structural divisions includes a drunk tank?',
412499: 'jamerlan: from the changelog of the opensource project "springrts" on GitHub just read: rename BaseSky to ISky (we may attract even more Mac devs now!)',
412500: 'philpirj@habr: every time you write int i instead of short i, hundreds of users are forced to buy an extra RAM stick',
412501: 'd0lboiob: By the way, she told me how a stranger attacked her.\nd0lboiob: Unexpectedly. Flew up from behind and attacked.\nd0lboiob: She didn\'t feel anything at first.\nd0lboiob: And then got really scared.\nd0lboiob: I got really scared for her too.\nd0lboiob: And she said it was a mosquito.',
412502: 'Daughter: mom, I bought a fur coat\nMom: for how much?\nDaughter: $$$$... )\nMom: and what did your husband say?\nDaughter: he doesn\'t know about it. ;)\nMom: and so he stays unaware, are you going to wear it under your bra?',
412503: 'Rezon: You\'ve got a level up, and I\'m crooked.\nRezon: I have a new code writing tactic.\nRezon: I write code, add crutches, then forget about them, see the crutches, delete the crutches, remember they\'re needed, write crutches...',
412504: 'ladivinatragedia: you know, I noticed: the fastest to reply to my letters is the mail delivery system (',
412505: 'xxx: Mom shoved her books "Correcting Deviant Behavior" and "Developing Schoolchildren\'s Intellectual Abilities" onto the fantasy shelf.\nxxx: Well, in principle, yeah...',
412506: 'Shaman: heard google is opening its social network\nskw: yep\nShaman: I\'ve long had the opinion that Pinky and the Brain are in charge there, and they finally created a working plan for world domination...',
412507: 'xxx: What happens if I, falling from the fourth floor, land on my head?\nel: You won\'t be able to ride a bike.\nxxx: Why?\nel: The coffin lid will get in the way.',
412508: 'tosha: At 16, to impress girls, I started drinking, smoking, and swearing.\ntosha: It worked.\ntosha: Now I\'m 23 and to impress girls more, I quit drinking, smoking and swearing.\ntosha: That works too )',
412509: 'XXX: Before, when I wasn\'t married and had no kid, kids crying annoyed me.\nYYY: And now?\nXXX: Now I listen calmly — it\'s not mine, it has parents, let them calm it down.',
412510: 'xxx: In Yandex\'s cache they also found Kennedy\'s killer, the Holy Grail, the Higgs boson and the second sock',
412511: 'ххх: Guess the riddle: "Hangs a pear, can\'t eat it!"\nууу: dunno\nххх: Let me explain. WHERE THE FUCK DID THE CRATE OF FRUIT GO PER INVOICE NNNN?',
412512: 'xxx: Didn\'t get lunch today, now I\'m confusing my phone\'s vibration with my stomach growling.',
412513: '<@[bETA]mEN> Domodedovo is seeking removal of the scandalous video with supervisory board head Kogan from the internet\n<@[bETA]mEN> removal\n<@[bETA]mEN> from the internet\n<@[bETA]mEN> okay!',
412514: 'fsrs: In Russia it won\'t fly (expired goods, delivery /etc), but overall it\'s very convenient, since in our region there\'s nothing to order except pizza.\nDr_Logic: muscle memory, scary thing, etc without the slash doesn\'t get written anymore? :)',
412515: 'xxx: if you x-ray my brain, it\'ll look like Swiss cheese\nyyy: What? Bad memory?\nxxx: no, fucked to hell.',
412516: 'Riff: launched WoW under wine - the exit button doesn\'t work. That\'s how Linux users die at their computers.',
412517: 'elio: email subject in my inbox: "(Konstantin) Greetings from PRC quickly deliver consolidated cargo tel ..."\nhGRrrrr: Frankenstein at home?',
412518: 'Today, the IT department head and the admin:\n\nHead: Mish, where\'s your screwdriver?\nAdmin: I gave it away.\nHead: But you burned it out recently.\nAdmin: That\'s why I gave it away.\nHead (thoughtfully): Aaaaaa... Cool gift... Don\'t give me anything.',
412519: 'Sign: Express print shop <...> Business cards in 2 hours\nI walk in: - Hello, I\'m interested in digital business cards\nLady: - We don\'t have digital business cards, only regular ones.\nMe: regular, is that offset?\nL: no, it\'s not on the printer.\nMe: well obviously it\'s on a printer. Do you have Xerox or Minolta machines?\nL: young man, we copy on the xerox. And we print business cards on the PRINTER, we told you!',
412520: 'ХХХ: Came up with a genius anti-theft device for cars! You need to install the windshield with prescription lenses. And the driver and passengers wear glasses with reverse prescriptions.\nУУУ: Yeah, then car thieves will bring on the job, besides a set of lockpicks, a set of glasses too, and before driving into the forest they\'ll put up signs "don\'t leave your car in a sunny clearing, it may cause a fire".',
412521: 'xxx: Management got concerned about computer security\nxxx: Started by taking the domain administrator password away from the administrators.',
412522: 'Sergey Maximov: Does anyone know if you can restore a recording on an old (worn-out) VHS tape? and where can you do that?!\nАндрей Ветров: Download it again\nSergey Maximov: Andrey, IT\'S A VIDEOTAPE!!! what do you mean download? )\nАндрей Ветров: I get it\nАндрей Ветров: You can\'t download the tape. But the video you can )\nSergey Maximov: where would you download my parachute jump at age 12?\nАндрей Ветров: Not on YouTube?',
412523: 'ххх: What are you up to?\nууу: Playing with my cat )\nххх: How I\'d love to be your cat (\nууу: Then you\'d have to be neutered.\nххх: What a high price... :\'(',
412524: 'xxx: Modern parents increasingly hear from their kids the question: "Mom, what\nis... ah, found it already".',
412525: 'question: I\'m 16, and my girlfriend is 14, how can we satisfy each other in intimite terms?\nWe could easily get to sex, but she says she\'s not ready yet.\nShe\'s slightly afraid of something new. So tell me how we can satisfy each other?\nAnd answers like - "You\'re too young, you\'re still kids" don\'t send!\nThanks in advance\nххх: Play Warcraft together',
412526: 'aaa: found a website online. "upload your photo - and the photo will age!"\nbbb: Dorian Gray Inc?',
412527: 'Was visiting relatives in Germany. For those who don\'t know - there almost everywhere water is metered and it\'s not cheap, like electricity.\nMy aunt says indignantly after my story about life in Russia:\n- You think our officials don\'t steal? They steal like crazy! There\'s one, imagine, who set it up so he DOESN\'T PAY FOR WATER!',
412528: 'xxx: A kid\'s question. Why are cold water taps marked blue and hot water red, if a blue-spectrum photon\'s energy is about one and a half times a red-spectrum photon\'s energy?',
412529: 'xxx: your morning starts at noon!\nyyy: Not true! My morning starts at 12! And lunch, fair and square, six hours later, at 18.',
412530: 'xxx: if I constantly keep myself within bounds, I\'ll turn into a portrait',
412531: 'ххх: here in one restaurant the waitresses look like they\'re in sailor moon costumes\nyyy: I BRING THE SALAD IN THE NAME OF THE MOON!',
412532: 'doz10us: do your relatives also have the habit of coming at the coolest moment of the movie and spending 20 minutes sorting out some bullshit?',
412533: 'xxx: My sister works as a taxi dispatcher. Takes an order.\nA girl says in a cheerful voice: I\'m at such-and-such place. Then a low pleasant male voice cuts in and says rudely but tenderly: Kiss me, bitch, kiss...\nlower...\nlower...\neven lower...\nthen male moans\nthen female\nand short beeps. The girl never went anywhere.',
412534: 'From LiveJournal:\nthey told about the selection of kids for the swimming section. The coach asked the kids to jump into the water from the board one by one and swim across the pool to the ladder, to see how each moves in the water. Everyone swam as best they could, and one boy sank to the very bottom, and walked along the bottom to the ladder, climbed up. He couldn\'t swim, by his words, but an order is an order :)',
412535: 'xxx: It pisses me off when flies rub their front legs. Like they have a plan and I\'m fucked.',
412536: 'xxx: I just had a security perimeter breach.\nyyy: ???\nxxx: The wind blew the paper with the admin password out the window!',
412537: 'Was talking to a girl on the phone\nMe: what station do you live at?\nHer: won\'t tell!\nMe: why?\nHer: what if you come and steal me?\nMe: but you want that!\nHer (doomed): but you won\'t steal me! :(',
412538: 'From an APCS specialist\'s notes:\nObject-oriented programming - came to the object, oriented yourself and started programming.',
412539: 'she: I\'ll step away for 20 minutes. My water has boiled.\nhe: mmm... do you cook well? something tasty?\nshe: Yes! My signature summer dish, washing up!',
412540: 'buyer: hello, do you have external FDDs?\nmanager: yes, what capacity?\nbuyer: 1.4 MB\nmanager: 0_0\nbuyer: do you know what a floppy disk is?\nmanager: no\nbuyer: can I get someone older?',
412541: 'xxx: My sister taught the parrot to meow.\nxxx: I\'ll throw out both!',
412542: 'Sergio: So who would you like to be in another life?\nКиСуНя: Dunno, some deity, preferably Indian. So all the people of India worship me.\nSergio: You want to be a cow? You\'ll manage that in this life too.',
412543: 'Aquarius\nToday you\'re simply a relationship guru! Both romantic and business ones. James Bond is jealous!\n\n0:26: Сонечка: Ahaha) really true!\n0:27: Сергей: sonechka, did your horoscope start coming true at half past midnight?\n0:29: Сонечка: yep))) I\'ve been a relationship guru for half an hour now)))\n0:33: Сергей: I can picture how you do it, sitting on VK',
412544: 'Петр: Zhen, where are you hanging around? I want to talk, and you keep disappearing every 5 min!\njhonny10: I have a terrible disease (\nПетр: Which one?\njhonny10: Post-Starbucks syndrome!',
412545: 'xxx: After 10 minutes of searching the waitress honestly admitted that they don\'t have a bottle of wifi.',
412546: 'ххх: What kind of fucking horror movie is this if you don\'t even want to hide your leg under the blanket?',
412547: 'Women\'s forum\n\nАлёна: I have an eternal question — why the hell do men wear WHITE TERRY socks with black trousers and black shoes?\nin summer.\nto a wedding.\n\nВера: That\'s the going-out pair! And black ones are for every day',
412548: '<Paфaэль>: We\'re two wings of one bird!!!!\n<friko>: Paфaэль : and that bird is a penguin!',
412549: '<xxx> "In the capital\'s pond they caught a piranha, a crocodile and a tractor"\n<xxx> Imagine: you\'re swimming, and behind you a piranha...\n<xxx> A crocodile...\n<xxx> And A FUCKING TRACTOR!!!!!',
412550: 'xxx: cool to swat a wasp mid-flight with a ruler!\nxxx: not fun later finding it on the chair with your ass (',
412551: 'Иван: fine, screw it. when you come to the base, what do you bring?\nМакс: Natashka brings blankets =)\nИван: then eat your blankets\nИван: two moths',
412552: 'denned: I think it\'d be logical if doctors talked the way they write. For example:\n- You have фылдииишщеацоз and need to take уцииииишшщцукалин.\n- Excuse me, what? I didn\'t understand a word you said, please write it on a piece of paper.\n- Easy, here...(writes)\n- Doctor, you\'re messing with me!',
412553: 'xxx: you look cool in a skirt\nууу: thanks )\nxxx: almost everyone\'s in skirts today, is it a flash mob?\nyyy: a flash mob. called "+34"',
412554: 'luckyman: Bought a Men\'s Health magazine. Saw an interesting article - how to properly have sex in a car. Opened the magazine, saw - how to properly position yourself in a Porshe911. Closed the magazine:(',
412555: 'My stomach hurt, they brought me to the hospital by ambulance, sent me to be examined by an old lady gynecologist.\nOld lady: Are you sexually active?\nMe: No.\nLooks at me suspiciously.\nOld lady: Sure?\nMe: Yes.\nOld lady: Really sure?\nMe: Yes!\nOld lady: Noted: "Denies".',
412556: 'qspm: mosquitoes are totally out of line. They land on the edge of the bed, at a distance where I can\'t hear them yet. and walk up to my legs, bite and then quickly fly away =(',
412557: 'XXX: It seems strange to me to catch a person who hacked via public WiFi. Say I\'m sitting at the Mac or on the beach for example — there\'s free WiFi and my MAC address is all FF, so how do you find me? arrest everyone with red swimming trunks?\nYYY: with red eyes',
412558: 'mom admiringly looks at the cat: how does he know when he\'s hungry?\nme: same way you do\nmom: well I, uh, I just love to eat',
412559: 'liquid sky: for days now my classmates and other guys from the student brigade have been rehearsing in the assembly hall, which is right under my office\nliquid sky: I\'d like to wish them to have sex the way they sing\nliquid sky: NEVER HIT IT',
412560: 'ххх: the dacha neighbor asked me to come hammer a couple of nails, like to hang pictures. seems simple, BUT!\nххх: dunno what kind of wall it is. concrete? then we bring the hammer drill. maybe wood? or drywall? bring the screwdriver too. and a set of drills. and a set of dowels. and a coil of extension cord. and a spare battery. and a level — pictures should hang straight, right. and a bunch of other junk.\nххх: in short, I wrapped myself up like a commando, I go out. coming toward me is Seryoga — where are you going? - to Irina\'s, I say, to hammer a couple of nails. oh, he says, I\'ll come too, I just borrowed her vacuum cleaner recently, need to return it. we walk through the village together, me with all the gear and him with the vacuum cleaner.\nххх: Irka opens, clinging to the doors laughing\nххх: says, the kid is just watching Ghostbusters\nххх: she says, when I saw you in the window to that music, I almost pissed myself',
412561: 'ххх: I\'d gladly parabola))\nууу: and I\'d probably gladly hyperbola',
412562: 'xxx: they say the isus series laptops are the best. can anyone tell me if that\'s true? if so, recommend a good model\nyyy: The best model in this series is hristos',
412563: 'Standing at the station, waiting for my friend. A granny comes up (Б):\nБ: Dear, can I call my daughter from your phone, we can\'t meet up\nMe: Sure, granny. Dial the number\nБ: Just PRESS TWO AND HOLD IT',
412564: 'xxx: sex with me is just an excuse to wash up for you.',
412565: 'xxx: I think the angle grinders in my building are in mating season and they\'re calling for a partner. (',
412566: 'me: give me a blowjob.\nwife: honey, my head already hurts, and you\'ll be knocking on it from the inside.',
412567: 'from LiveJournal katriona_cat:\n\nAnd today is also Navy Day.\nOn this occasion the streets are teeming with young men in striped sailor shirts and sailor caps. One such guy is standing by a store, drinking beer. And I\'m walking by. And then he turns slightly and what catches my eye is... uh... a non-regulation part of the uniform. On a perfectly army belt there\'s a pink Hello Kitty buckle.',
412568: 'вова: Why aren\'t you sleeping?\nLero_Ok: it\'s still early\nLero_Ok: and you?\nвова: I\'m on duty\nLero_Ok: wow, cool\nLero_Ok: is it pretty?\nLero_Ok: so why aren\'t you sleeping?',
412569: 'ххх: I walk into the office, Natasha is all glowing with joy\nххх: she says - imagine, I won on the radio, I sent an SMS with the answer to the question and won a certificate to a veterinary clinic\nххх: I ask what the question was\nххх: who can reproduce different sounds of other animals when scared, options: deer, ferret and ermine\nххх: well I thought, she says, - birds can do everything! ....and wrote ermine',
412570: 'Yesterday at work.\nA girl from the neighboring department walks in, I listen to the dialogue:\n\nххх: we have cake, let us treat you.\nyyy: Thanks, but I won\'t, I\'m on a diet.\nххх: But our cake is slimming, eat it, and then in half an hour you sit and think - "I\'m losing weight..."\n\nOnly one question was born in me: WHERE ARE YOU SITTING?!',
412571: 'Кузя: it\'s so cool when you get in the shower to wash your feet, put them under the faucet, turn on the water, and it, fuck, pours from the showerhead onto your head!!!',
412572: 'LiNED: at first they tell you you\'re the navel of the world, but after three days of playing you realize this world has way too many navels',
412573: 'xxx: Why do men hate other people\'s beautiful women in other people\'s beautiful cars so much?',
412574: 'xxx: where did you put the keys?\nyyy: look on the shelf\nxxx: which one?\nyyy: what are you stupid! the one the keys are on!',
412575: 'xxx: everything was great, bought shashlik, ate our fill, played ball to our hearts\' content\nууу: don\'t you want to add punctuation? because it scares me how you "ate into the ball" and "stocked up on shashlik"',
412576: 'ххх: Our elevator\'s lamp turns on once every 20 seconds - time to replace it, but the residents can\'t be bothered. And on the back wall there\'s a mirror, but covered with cardboard, since there are renovations in the building - so it doesn\'t get broken when furniture is moved. So I called the elevator, digging in my phone, got in, pressed the button.\nThen for some reason I turned around and suddenly the lamp turns on and lights up out of the darkness a creature with huge eyes staring at me.\nThe damn mirror finally got uncovered, I almost had to change my pants...',
412577: 'xxx: And if there\'s no sex with one and none in sight, and there is with the second, who am I cheating on?',
412578: 'Dirol2d: Damn, Katya dumped me\nBananaToaster: Come on! You\'ll have a hundred more like her!: )\nDirol2d: A hundred more?! God, what for?',
412579: 'xxx: With your intuition you could only guess which hand the watermelon is in',
412580: 'ХХХ: We really are like "us" - one thing is written on the fence, but behind the fence there\'s firewood.\nХХХ: Today I was driving to work - saw them unloading a van.\nХХХ: The van says "Ice Cream", but inside there\'s vodka.',
412581: 'I live in the south. Right now we have heat around 40 degrees. My grandma from Moscow called to wish me happy birthday, wished me all the best and "...may you always have a cloudless sunny sky above your head and may rain never darken your life!". I tell her "Thanks, given the specifics of our region, you practically just cursed me!"',
}

def main():
    src = {}
    order = []
    with open(SRC, encoding="utf-8") as f:
        for line in f:
            line = line.rstrip("\n")
            if not line.strip():
                continue
            r = json.loads(line)
            src[r["id"]] = r["text"]
            order.append(r["id"])
    assert len(src) == 1000, len(src)
    rng = order[400:500]
    assert rng[0] == 412482 and rng[-1] == 412581, (rng[0], rng[-1])
    assert len(EN) == 100, len(EN)
    with open(OUT, "a", encoding="utf-8") as f:
        for i, rid in enumerate(rng, start=401):
            assert rid in EN, rid
            ru = src[rid]; en = EN[rid]
            assert en.strip(), rid
            assert en.count("\n") == ru.count("\n"), (rid, "newlines")
            toks = re.findall(r"<[^<>\n]*>", ru)
            for t in toks:
                assert t in en, (rid, repr(t))
            f.write(json.dumps({"id": rid, "ru": ru, "en": en}, ensure_ascii=False) + "\n")
    print("OK: appended 100")

if __name__ == "__main__":
    main()
