#!/usr/bin/env python3
"""Append chunk_30 records 601-700 (ids 412683-412785)."""
import json, os, re

BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC  = os.path.join(BASE, "chunks", "chunk_30.jsonl")
OUT  = os.path.join(BASE, "out", "chunk_30.jsonl")

EN = {
412683: '<xxx> Vilnius mayor Arturas Zuokas demonstratively crushed an illegally parked Mercedes with an APC\n<yyy> In the next episode the Vilnius mayor will shoot three guys drinking beer on a playground.\n<zzz> God, save us from the missile mayor…',
412684: 'prosto_dzen: you\'ll learn that the server is called the fall guy in vain, you\'ll see that it stands in the center of our server room',
412685: 'xxx: Running from ticket inspectors in the commuter train with a 4th iPhone and 2nd iPad, that\'s all of Russia and Moscow in particular.',
412686: 'AnatolyB: Tolkienists are bullshit!\nAnatolyB: Like, Stroustrup invented C++, and so much poetry, prose and fables have been written on it!\nMezomish: And how many jokes...\nХыбло: to be fair - most of what\'s written in C++ is crap.',
412687: 'yyy:YOU CAN\'T EVEN LISTEN TO ME\nxxx:I\'m listening\nyyy:I\'m not going to say anything',
412688: 'xxx: Pritveltt!) I\'m rihgt now goonna go pikc up Dismk!\nyyy: And peace and prosperity to you, honorable native',
412689: 'MaxEd: So how is it, still no internet?\nNevermind: they gave something\nNevermind: it\'s just like the internet, only slow',
412690: 'Went to bed with my wife, eyes closing on their own, silence, darkness and then a mosquito appears and starts buzzing annoyingly and flying around somewhere nearby.\nWife: Just let it bite you already, otherwise we won\'t sleep half the night, what\'s it to you. You\'re big, and the mosquito is hungry!',
412691: 'Елена: I just can\'t calmly call the formidable Kharkov installer guy who has as his ringtone the melody that he\'ll be my baby )',
412692: 'fiona: they put a powerful coffee machine on the 3rd floor, built into a cabinet. it has so many buttons - by the time I made myself a latte, I think I hacked the Pentagon',
412693: 'Gal: I\'m on vacation starting tomorrow.\nДиман: lucky you, going somewhere?\nGal: yep, first to Thailand, then Haiti, then Hawaii, Gubachevskaya dam, Egypt, Cyprus, Volga, Goa, Kostroma sea, tour of Africa...\nGal: cross out the odd one out )',
412694: 'xxx: The printer says "close the tray". What does that mean?\nyyy: Guess\nxxx: The cartridge ran out?',
412695: 'In the university cafeteria:\nStudent: Is the puff pastry from yesterday?\nCafeteria lady: Warm it up - it\'ll be today\'s',
412696: 'Rei: wiiii, I have free wi-fiiii on my balcony!\nRei: something tells me I\'m going to hate this winter...',
412697: 'xxx: fallout new vegas. monsters run around the desert, after which you\'re scared to go out into a dark hallway at night. you can smoke, and it improves concentration. booze on anything that burns, and you become more charming. steal, rob, loot and rob graves. a huge selection of drugs that make life much easier. play all gambling games. waste old people, women and children, torture, burn alive, dismember, beautifully smash into small pieces, plant explosives in pockets, crucify and commit genocide.\nand cannibalize.\nbut damn, when it comes to sex - twice in the whole game - the screen bashfully goes black. of course, such a shock for a fragile psyche.',
412699: 'xxx: once you have a husband, he\'ll fix all these things for you, reflash them...\nyyy: love is blind, you\'ll fall for a humanities major',
412700: 'ххх: I mean, if there\'s no electricity at all, what will we do then? A teacher will teach, a doctor will heal, an IT guy - ...\nууу: Study and get healed',
412701: 'nx: we\'re going to the field. come with us\nHardMan: to play soccer?\nnx: golf\nHardMan: what field are you going to?\nnx: the corn field, fuck! Are you coming or not?',
412702: 'Соболь: I think god, if he exists, is not a moderator but a sysadmin. Like: guys, I set everything up for you - the sun, the planet, the periodic table, DNA, wi-fi. Stop being dumb and bothering me with your petty idiotic requests like world peace or fiery rain on gay parades.',
412703: 'Another OS argument on Habr\n\nunhappy: I confess, but I\'m a windows supporter\nganggwerty: Is that somehow related to your nick?',
412704: 'xxx: I was gifted whiskey. It costs five thousand. I\'m thinking of drinking it with cola as usual, but I want some expensive cola...\nyyy: Go to the movies :)',
412705: 'xxx: Nobody knows what tomorrow may bring!\nyyy: Tomorrow will be Friday',
412706: 'ххх: Facebook reminds me that my wedding anniversary with my husband is coming up\nххх: and asks if I\'ll go to it\nххх: *SCRATCH* well now I don\'t knooow...',
412707: 'Anya: Mom, today at school they told us that Achilles had a weak spot - his heel!\nMom: Yes, dear.\nAnya: And I watched "Troy" and thought - maybe what killed Achilles wasn\'t the arrow driven into his heel, but the five more arrows in his chest after that?',
412708: 'hysterical:\ntoday I worked as a mail server\n\nhysterical:\nat work our mail server died\n\nhysterical:\nso I ran around all the offices between the two tech buildings with a flash drive asking "does anyone here need to send anything by mail locally?" If the answer was yes, they\'d throw a file on my flash drive and I\'d carry it to the recipient :D',
412709: 'mixer: so I woke up and told you my ass hurts. and others have TWITTER for that, fuck',
412710: 'Went with friends to the Kubana festival. For those who don\'t know: the festival takes place on the Black Sea coast 60km from Anapa. Found a phone on the beach and as decent citizens decided to return it to its owner. Found the number "mom" in the contacts and called. Then the dialogue.\n- hello, I found a phone, apparently it belongs to your son or daughter.\n- yes, it\'s my son\'s phone, where are you?\n- ummm, well like... on the beach, at a rock festival near Anapa.\n- MY SON IS IN ANAPA?!?!?!?',
412711: 'Rambler-News\n"Specialists from CA Technologies reported on malware that records smartphone owners\' phone conversations on the Android platform."\n\none of the comments:\nThe virus is called: "And what did he say? And she? And what did he? So? Wow! And he?"',
412712: 'xxx: Eh, friends, I got hooked on the new Mortal Kombat. Got hooked with pleasure. What can I tell you - that\'s how old age comes.\nxxx: I can\'t beat these parasites. But my son can.\nxxx: When I give him technique tips.\nxxx: I feel like Rocky Balboa gone into coaching...',
412713: 'xxx: and then a cockroach climbs onto the first desk\nxxx: the teacher looked at it and declared "Today you were supposed to have Proust, but since this is the case - let\'s talk about Kafka".',
412714: 'typhlosion: I wonder what fans of the game "gorodki" think when looking at Stonehenge?',
412715: '<Wolfess> Do you have problems in your personal life?\n<Kriegwolf> Yes.\n<Kriegwolf> My personal life is limited to my hands.\n<Wolfess> What, both at once? ;)\n<Kriegwolf> I TYPE ON THE KEYBOARD WITH THEM, YOU PERVERTED ANIMAL.',
412716: 'levfox: I was standing listening to music in my headphones in the rain. Beethoven was playing. A stranger came up to me and said I was strange. I said I was listening to music. He asked if I was messed up, showed his badge. I gave him the headphones with the music. He listened! Then he looked at me and apologized.',
412717: 'Romantic: New element - budgetium-2011. Half-sawing period - 1 month.',
412718: 'ххх: Imagine a bucket of gasoline.\nххх: Does it look like a car because it has gasoline in it?\nууу: no\nххх: Now a bucket with wheels. Does it look like a car?\nууу: no :)\nххх: And with headlights, an antenna and a flasher?\nууу: no\nххх: then why, slapping a spoiler on your bucket, do you call it a car?',
412719: 'TAPAKAH*[AW]: to understand a maniac, you must become a maniac! think like a maniac, walk like a maniac, dress like a maniac!\nTEHb: and you, I take it, want to understand clowns?',
412720: 'xxx: At my job the scariest phrase is: "guys, what date do we have a database backup for?"',
412721: 'xxx: something lately I\'ve been noticing that my boss sends everyone to me\nxxx: feels like I\'m a dick',
412722: 'xxx: hiiiii!!!!!!!!!!!!!\nxxx: where were you?!!!!!!!!!!!!!\nxxx: what are you doing, why are you silent?!!!!!!!!!!!!!\nyyy: I\'m wondering, did you copy-paste those 13*3 exclamation marks, or type them one by one?',
412723: 'zip-your-lip: just saw an ad - "children\'s hell N200 needs a teacher"\nboro-bvb: number 200...\nboro-bvb: that\'s not a typo. My mom took me THERE 17 years ago...',
412724: 'I remember my classmate performed at student readings, she was asked a question, she answered brilliantly: "Coffee — it, of course, is masculine..."',
412725: 'From a forum:\nxxx: That\'s nothing... I once read that truly quality sound is only possible if the current in the grid comes from a hydroelectric plant, since "everyone knows that a hydroelectric plant generates much more audiophile energy than thermal or nuclear ones".\n\nyyy: Yeah! Hydroelectric plants give a more natural, organic sound, thermal plants a warmer sound, solar panels a brighter sound, wind turbines a lighter and airier one, and nuclear plants add more modern electronic effects.',
412726: 'ххх: How do you feel about love for three?\nууу: You mean the cat who\'s watching?',
412727: 'xxx: I\'ve never brewed such potent pu-erh\nxxx: wise ancient Chinese thoughts are crawling into my head\nxxx: only I don\'t understand a damn thing in Chinese (',
412728: 'xxx: pity there are no sleeping bags that could transform into a guitar case and a jacket when needed ))\nyyy: yeah, and then inflate into a chick too if needed',
412729: 'Ххх: So, Anka\'s pregnant after all. We come to her parents. Mom immediately hysterical, screaming, starts dialing dad\'s number. He sternly says: "I\'ll come - I\'ll deal with it". We wait. I even got a bit scared.\nХхх: Her dad arrives, by the way a military man, though retired. And in a rolling bass asks Anka: "Are you going to give birth?"\nХхх: "Yes", says Anka crying. Daddy thinks long, scratches his head (apparently counted on a different option), and finally delivers just one word:\nХхх: "Fuckyeah". And leaves. Truly - fuckyeah.',
412730: 'xxx: in the morning I, like a normal person, sleep, and Anton, like a normal person\'s brother, plays',
412731: 'Den Stranger: I\'ll carry you in my arms!\nФрео: You won\'t lift me :)\nDen Stranger: I meant figuratively :)\nФрео : You won\'t carry me :)',
412732: '<Хуанита> A year ago my son and I were at the beach. And then the kid, having stared at the tits around, runs up and asks: Mom, I won\'t grow those, right? (looking anxiously into my eyes)\n<Хуанита> I\'m calming him as best I can: What are you talking about, sunshine, only aunties grow those. And then out of the water comes, damn, an uncle... with tits twice as big as mine',
412733: '111: Recommend a 50-inch plasma\n222: I recommend!!! great thing',
412734: 'Our news always amazed me. A report on a famous person\'s death usually starts with the words: "And now the sad news". So before that they spent 15 minutes talking about disasters, terrorist attacks, cataclysms, and that wasn\'t sad at all. But now such sadness, such sadness...',
412735: 'xxx: Yul, here\'s the thing\nxxx: basically it turned out like this\nxxx: you\'re a desman',
412736: 'Tech support daily grind:\n\n-Do all the letters in your password lowercase?\n-Yes.\n-So there are no capital letters?\n-There are.',
412737: 'DRIV3R: Special signs of the guys also turned up by the way: on the back mudflap there\'s a panther drawn, and the guys ride in a red helmet, one for two...\nkazash: isn\'t it tight for them?',
412738: 'рУкИвЖоПе: As a kid I was taught to tilt my soup bowl away from myself so as not to spill. Apparently, before sitting down to eat at my laptop, I should\'ve reconsidered my priorities.',
412739: 'Лери: bought a door?\n<Злой>: Didn\'t buy, but chose one, wait 3-4 weeks, since the frame is non-standard\nЛери: non-standard - that\'s round, right!!!??\nЛери: tell meee!!\nЛери: we\'ll be like hobbits??!!!!',
412740: 'СТиХиЯ: and I\'m in a new dress, so beautiful, tight. I feel like a Barbie doll in it...\nНевеста: just as beautiful and slim? ;)\nСТиХиЯ: no. to take the dress off, you have to tear off my arms... (',
412741: 'lucas: listen, dude\nlucas: how many messages should a girl send after the phrase "I won\'t say another word to you, goodbye"?\nlucas: because this time it\'s only 14 so far, I\'m starting to worry',
412742: 'ххх: Picked up Tanya with the little one from the maternity hospital, came home, a couple hours later a call: "This is the maternity ward bothering you. Did we definitely give you a boy?"',
412743: 'Mail.ru Q&A\nxxx: Recommend some program for photoshop ))) I\'ll be waiting, thanks in advance:)\nyyy: Photoshop. The best program for photoshop.',
412744: 'When you\'re sitting at work and suddenly there\'s a moment when you\'ve already done everything and you realize there\'s nothing left to do, and there are still several hours until the end of the workday - don\'t rush to relax, most likely you fucked up something big somewhere!',
412745: 'xxx: I have a welding machine, I\'ll weld you shut, fuck!\nyyy: And can you weld anything besides pelmeni?))))))))',
412746: 'xxx: You know, guys sometimes like it when a girl has a tummy. A tummy is so plump and cute.\nyyy: Well I\'m not arguing with that... Only you don\'t have a tummy, Mash. YOU HAVE A HUGE FUCKING FAT BELLY HANGING ALMOST TO YOUR KNEES ANY GUY WILL DROWN IN IT STOP EATING BURGERS RUNTOTHEGYMYOUFOOL!!',
412749: 'Ticket pickup at the tour operator is on Kuznetsky Most. At the entrance they hand out an instruction sheet explaining how to get to the office.\nThe instruction:\n"Take the elevator to the 3rd floor, from the elevator go right, walk down the corridor to the door with frosted glass, go through it, take the elevator to the 5th floor, straight through the door, right, straight down the corridor to the stairs, up to the 6th floor and right, walk down the corridor to the next elevator, down to the 4th floor, the right door, straight and left and go into the hall on the left."\nDAMN IT, THIS ISN\'T EVEN MORROWIND!!!!!!!!!!!!!',
412750: 'ххх: we\'re just berries from different fields... I\'m from a minefield, and he\'s from a hemp field...',
412751: 'Zyf: I love my daddy!\nZyf: asked him to fix the nightstand, it opens really hard.\nZyf: so what does a real man do in this situation?\nZyf: he nails it shut and in a commanding tone says "don\'t touch it anymore".',
412752: 'ххх: I\'m feeling great actually )\nххх: Just could eat..\nууу: There\'s 220 volts there, don\'t.',
412753: 'ххх: we finally decided what to give Sanya )\nххх: our cat decided the matter\nххх: in the morning he pissed in his boots )))',
412754: 'zppln: ls\nzppln: sorry, wrong window',
412755: 'Ужас: don\'t you want to tell me something?\nРаффи: I do\nУжас: well: don\'t',
412756: 'xxx: my Gentoo compiles faster than your girlfriend',
412757: 'ххх: A priest just came to us, to bless the office. Still smells like kodilo. Or what\'s it called - kadilo?\nууу: Kadilo. Kodilo - that\'s your programmer.\nххх: He smells sometimes too (',
412758: 'xxx> By the way, came up with a way to leave work earlier!\nxxx> Half an hour before the work day I crank torrents to max. The director starts getting sad that his pages open slowly, and with the words "Fuck this internet. I\'ll read at home. WHO NEEDS A RIDE!" the office empties',
412759: 'Rat_S: he now works at our internet provider, walks around all important and tells everyone that he "gives the country google"',
412760: 'Басик: hi, how\'s it going?\nДимкО: awful, can\'t be worse.\nБасик: getting married?\nДимкО: worse...\nБасик: you\'re marrying off?!\nДимкО: fuck you!',
412761: 'xxx: When I was asked to come up with a name for an automated control system (I\'ll skip the purpose), I proposed "centralized complex objects control system". xxx: The name stuck as the working one. They didn\'t appreciate it right away :)',
412762: 'xxx: The Ministry of Health clearly chose the target audience and the tool for fighting smoking.\nxxx: On a block of pink, thin women\'s cigarettes, on 30% of the block\'s area, in large black font it says: Smoking leads to impotence.',
412763: 'D: Daamn, it\'s raining there(\nP: Are you made of sugar? )\nD: No, fuck, I\'m a gremlin! I\'ll start multiplying!',
412764: 'ххх: The thing is, a girl\'s computer broke, presumably the video card.\nI\'m leaving tomorrow morning on a business trip for an indefinite time.\nHer computer is at the office downtown.\nWho\'d agree to diagnose the computer? Make sure it\'s the video card and buy a filler card, used is fine.\nууу: How will the girl "thank" for the computer repair?\nххх: Well with money, if it\'s hard to guess, but if that\'s not enough - she can add a gorgeous right hook. I checked myself back in the day, there\'s like 150 kg in it...',
412765: 'oira666: lawyers in commercial companies are specialists in fighting Russian legislation',
412766: 'From a forum:\nxxx: The time and date keep resetting on the computer.\nyyy: Change the battery.\nzzz: The battery on the motherboard, mind you, or he\'ll go tear apart the central heating now.',
412767: 'One of the brightest phone conversations of my life.\n- Good afternoon, this is the security agency "......", we guard you under contract. And tell me, where are you located?',
412768: 'ххх: Giving away 40 kg of zucchini to strong hands.',
412769: 'Wadem: If I catch you with a man when I get back from my shift, I (shakes finger)... I!.. will join in!',
412770: 'yuvolgin: Here you go, Cinderella, a controlling stake in AVTOVAZ, but remember — midnight struck there forty years ago.',
412771: 'xxx: Listen, what\'s the name of that guy at the bank you slept with to get the loan?\nxxx: Oops, wrong window!\nyyy: no worries\nyyy: good luck with the loan!',
412772: 'xxx: and I don\'t want to get married because I have a beautiful photo in my passport',
412773: 'AsTi:\nharsh everyday life of an organizer\n"OH, I NEED TO ORGANIZE A CONCERT! let me call our poor musicians who always beg for guest passes to all parties, write every week and beg to perform everywhere and who have no money for rehearsals"\n- hello, poor musicians? would you perform this weekend?\n- oh, you know, our bassist is in Hawaii, vocalist in Thailand, drummer in the madhouse, and I\'m digging garden beds, no time',
412774: '*К@тёноК*: How do I make my eyelashes so they\'d reach my eyebrows?\nSerg: Grow your eyebrows!',
412775: '<gol>: Made a song in SoundForge with very quiet volume, and after 6 seconds the Private porn studio intro loudly, and dropped it into all social networks as a "childhood song" during work hours.\n<gol>: Three promised to beat me up, one got really offended.',
412776: 'xxx: how long does it take your kettle to accelerate to 100 degrees?',
412777: 'Marsiano4ka: Vasily is truly an irreplaceable programmer at our company\nMarsiano4ka: nobody understands his code except himself ]:->',
412778: 'anne: how was your day?\ngiugliamilano: painted the dresser with lacquer, smeared the whole floor.\ngiugliamilano: washed the floor with solvent.\ngiugliamilano: green gophers convinced me that the stains washed off completely',
412779: 'Конфетка: I just can\'t stand stupid people.\nРита: and you shouldn\'t\nРита: you need to love yourself',
412780: 'Cool-Di: in our canteen, after 4 employees got poisoned, cardinal changes happened - they hung a sign on the door "wash your hands before eating"',
412781: 'Пеппер: Yesterday we talked about education, and Sveta described mine not as "unfinished" higher education, but as "slightly started"',
412782: 'zveroboy: she\'s a real admin. plucked her eyebrows with pliers yesterday...',
412783: 'ъъъ: Recently came to a doctor\'s house call with a sick head... to fix a computer...\nъъъ: As a result I fixed the computer, and she fixed me. Doctors are челодмины.',
412784: 'mika: How do I take a screenshot?\ndin: PrintScreen and paste in Paint\n...\nmika: Where\'s the fly?!',
412785: 'ххх: Say a number!\nууу: 5864!\nххх: Fuck, what\'s today\'s date?\nууу: Oh, I thought you meant randomly )',
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
    rng = order[600:700]
    assert rng[0] == 412683 and rng[-1] == 412785, (rng[0], rng[-1])
    assert len(EN) == 100, len(EN)
    with open(OUT, "a", encoding="utf-8") as f:
        for i, rid in enumerate(rng, start=601):
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
