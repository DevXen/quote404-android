#!/usr/bin/env python3
import json, re, sys

BASE = '/home/hatch/workspace/translation-work/bashim'
START, COUNT = 900, 100

en = {}
en[404971] = (
'No_Future: the customs office has a cool informatics manual\n'
'No_Future: lab "graphic editor Pain"'
)
en[404972] = (
'KiloBaks: a special game, figure out what the girl is offended about, and explain it to her )\n'
'KiloBaks: no attempts, one life'
)
en[404973] = (
'==Artes==: hey, I\'ll send you a photo, make me bald in photoshop, please.\n'
'FATALITY: why?\n'
'==Artes==: just curious, whether I have a birthmark under my hair or not...'
)
en[404974] = (
'Nazar: you not sick?\n'
'Nazar: nobody sneezing at work?\n'
'Alexey: Everyone\'s sneezing at work'
)
en[404975] = (
'xxx: I\'m sick of cooking dinner for myself. I\'m sick of cleaning my apartment. I\'m sick of waking up to an alarm clock. I\'m just sick of living alone.\n'
'yyy: you need the army, none of those problems there :D'
)
en[404976] = (
'xxx: There are girls - a pure angel, the embodiment of beauty, modesty and virtue. Until they open their mouths.\n'
'yyy: What, cavities? :D\n'
'xxx: Yeah, brain cavities'
)
en[404977] = (
'xxx: Yeah, the most popular male excuse nowadays\n'
'xxx: "Nooo, I don\'t watch it. Just raising my torrent rating, that\'s all!"'
)
en[404978] = (
'My mom: Do you have a harlequin?\n'
'me: What?\n'
'Mom: Well, a harlequin, on the computer?\n'
'me: What do you mean?\n'
'mom: Such a program.\n'
'me: Acrobat?\n'
'mom: Yes, acrobat.'
)
en[404979] = (
'[artemlight] (03:35:54) me and a buddy just now were chatting about obsessive-compulsive disorders)\n'
'[artemlight] (03:36:00) in general, we need treatment\n'
'[artemlight] (03:36:32) don\'t even know why. Either because all kinds of crap haunts me at night, and he doesn\'t step on cracks in the asphalt\n'
'[artemlight] (03:36:46) Or because we\'re chatting about obsessive-compulsive disorders at half past three in the morning'
)
en[404980] = (
'xxx: Need to close my sole proprietorship, who\'s done it, tell me where to start?\n'
'yyy: You go to the tax office, there they ban you by the SP'
)
en[404981] = (
'yyy : What people! Haven\'t seen you in ICQ for a year.\n'
'xxx : Sorry, accidentally deleted you from my invisible list, fixing it now ;-)'
)
en[404982] = (
'xxx: And my nephew is sure that the plot of a famous Russian fairy tale is based on the fact that Princess Vasilisa was bitten during a lab tour by a genetically modified radioactive frog...'
)
en[404983] = (
'xxx: only when the number of computers equals the number of family members does full understanding arise in the family'
)
en[404984] = (
'she: a very beautiful avatar! And that\'s you against the background of what?\n'
'he: That\'s the Kremlin. Have you ever been to Moscow?\n'
'she: nope, and you?'
)
en[404985] = (
'XXX: Damn. Epidemics, pirates.\n'
'YYY: the middle ages, mother of it'
)
en[404986] = (
'Metis: Getting old. 22 years, and such receding hairlines, hair\'s falling out. Tell me, where does it go?\n'
'NINZO: look in your nose\n'
'Metis: you bastard!!!'
)
en[404987] = (
'xxx: I hate it when you smoke...\n'
'yyy: And I hate it when you play on the comp!\n'
'xxx: Well admit it, when I approach you, I don\'t smell like a stalker!'
)
en[404988] = (
'xxx: hi )\n'
'yyy: hi )\n'
'xxx: need your help? :)\n'
'yyy: no )'
)
en[404989] = (
'xxx: I sneezed so hard that when I stopped, I almost fell asleep from relief'
)
en[404990] = (
'xxx: so there\'s enough chicken for everyone, it needs to have four legs \n'
'xxx: it doesn\'t fly anyway! :D'
)
en[404991] = (
'xxx: what do you do when you have nothing to say to a person?\n'
'yyy: I\'m silent'
)
en[404992] = (
'gregst: and earlier they used to take wipers off the windshield and carry them along, so they wouldn\'t be stolen, but now they stopped\n'
'badlogin: my grandpa still takes off mirrors\n'
'TakHaDo: and how, profitable?'
)
en[404993] = (
'death: and why didn\'t you sleep?\n'
'Lokki-Navigator: Practice pass/fail preliminary\n'
'death: were you praying?'
)
en[404994] = (
'she: damn, I already want tomorrow morning so bad\n'
'he: and what\'s tomorrow morning?\n'
'she: tomorrow morning I\'ll wake up at 6 am to the alarm\n'
'he: why, tomorrow\'s Saturday!\n'
'she: I\'ll turn it the hell off, roll over to the other side and fall asleep again till noon\n'
'she: suuuuch blaaaiss!'
)
en[404995] = (
'xxx: So how much do you earn?\n'
'yyy: But I do fuck-all!'
)
en[404996] = (
'xxx: and actually, Sasha, though he\'s shit, but if you dig deeper, a very smart, educated, all-around developed person with a rich inner world opens up.\n'
'xxx: only who\'s going to dig in shit, except when drunk?'
)
en[404997] = (
'xxx: Listen, I get how our new generation differs from the old one, but how do the foreigners differ?\n'
'yyy: Basically the same way, only with them the new generation also watches Star Wars episodes 1-3, and the old one 4-6.'
)
en[404998] = (
'We\'re talking about house colubrids (the snakes).\n'
'xxx: Yeah indeed. Especially the male is good. A handsome fellow...\n'
'xxx: You forget to close the lid, then you spend 20 minutes luring it out of the vacuum cleaner pipe'
)
en[404999] = (
'xxx: Anya, do you have boobs?\n'
'yyy: Yes! Bigger than Semenovich\'s!\n'
'xxx: Three?'
)
en[405000] = (
'he: Eeeey! did you go to sleep? And what about the erotic conversation?.. )\n'
'she: right now the only thing I can do to you is brain-f*ck...\n'
'she: does that count as erotica?'
)
en[405001] = (
'A cry of the soul in a social network:\n'
'xxx> Sick of the kiddies!!! Where are you, adult 13-year-old guys?!?!'
)
en[405002] = (
'From Google Q&A\n'
'Yippo: How much faster does a carburetor engine wear out if in winter you start the car and drive right away on "choke"?\n'
'Yippo: Can you even do that?\n'
'TimXX: I tried that. The engine lasted three days.\n'
'TimXX: For two days I drove fine, and on the third a dump truck drove into my face, engine\'s dead...('
)
en[405003] = (
'RDT:\n'
'haha, cool to write a chick that you\'re small, bald and fat but have an Infiniti and hear from her that the main thing isn\'t looks, but the soul )'
)
en[405004] = (
'xxx: in Spain there\'s a city Son-Servera. A quiet town, probably )'
)
en[405005] = (
'xxx: as experience shows, the best flu prevention is a sincere desire to catch it'
)
en[405006] = (
'xxx: I have an upset stomach \n'
'xxx: It\'s upset that I don\'t feed it )'
)
en[405007] = (
'<_MorkoffKA_> damn. everyone asks what to gift me for my b-day, and then they get offended that I tell them straight...\n'
'<Skeleton Princess> people think you\'ll ask for shower gel'
)
en[405008] = (
'*** KABAN changed name to Olenka.\n'
'123: :-D\n'
'Olenka: what\'s funny?\n'
'Olenka: I\'m his wife'
)
en[405009] = (
'xxx: I work in a madhouse\n'
'yyy: tell us\n'
'xxx: me and Seryoga sit in a separate office, coding quietly, not bothering anyone. and in the next one, sit the newbies, five people. from us to them a door directly and never closed, they constantly run to us for advice. \n'
'xxx: just now amid the silence from the newbies\' office a wild scream "Triple hurrah for the Great Docs on Java API!!!" and immediately in chorus "Hurrah! Hurrah! Hurrah!" and silence again'
)
en[405010] = (
'xxx: black-and-white film is good for photographing naked nature\n'
'xxx: if it, of course, agrees'
)
en[405011] = (
'Alek: Good evening rather!\n'
'Kot: And here\'s Alek, jumping in and out of the skype chat a hundred times\n'
'Kot: Alek, why were you jumping in and out of the chat all yesterday?\n'
'Alek: against\n'
'Alek: I have yota on my laptop and skype is automatic\n'
'Alek: maybe it was jumping there? :)\n'
'Kot: Ah, so you were opening and closing the lid?\n'
'Kot: what, were you baking waffles with the laptop there?'
)
en[405012] = (
'torrent forum about a movie:\n'
'\n'
'Mishlen: guys, I can\'t even open it! what\'s the problem, who can tell? it says - unknown format.\n'
'zonez: lucky you, I watched it'
)
en[405013] = (
'she: now you\'re the most precious to me!!!\n'
'he: and how much already?\n'
'he: damn\n'
'he: and why is that?'
)
en[405014] = (
'xxx: Do they still use floppies at your work?\n'
'yyy: Yeah. They came in handy for us last year when there was an eclipse.'
)
en[405015] = (
'From a forum;\n'
'Funny or not, but father told a story. In his youth, working as an airfield military doctor, he assembled one airfield technician in pieces. The guy decided to make himself an auxiliary emergency braking option for his motorcycle. He attached a parachute bag to the trunk, tied a rope to his leg that pulls the ring and rode out onto the concrete road. Accelerating to max (how much does an IZH Planeta-5 have? probably 120 somewhere) he yanked with his leg. The motorcycle stopped dead, and since nobody canceled physics, the speedy racer flew onward'
)
en[405016] = (
'Your Love: come home with a friend\n'
'Your Love: tell mom - everything\'s fine, we\'ll be quiet\n'
'Your Love: he\'s like a girlfriend to me\n'
'Your Love: mom says to me: I also once brought myself a girlfriend\n'
'Your Love: me: and who was that?\n'
'Your Love: her: your dad'
)
en[405017] = (
'Ksu: I was sixteen, and I was really shy to tell guys that I was a virgin.\n'
'Jog: And when I was sixteen, I was shy to tell guys that I wasn\'t a virgin.\n'
'Cat: Damn, and at sixteen I was only shy about still having Windows 98.'
)
en[405018] = (
'She: please help.\n'
'He: yes\n'
'She: I have two comps. need to share the internet. have a switch, but the wires from the switch are very short. What to do? Only explain as if I were a dumb girl\n'
'He: buy a network cable and that\'s it\n'
'She: I asked to explain like to a dumb one.\n'
'He: well then pull harder'
)
en[405019] = (
'xxx: Sasha waited for me by the entrance for 40 minutes yesterday\n'
'xxx: confessed his love\n'
'xxx: that it\'s his first time like this\n'
'yyy: and what did you say?\n'
'xxx: nothing. my mouth smelled of garlic ('
)
en[405020] = (
'xxx: what\'s next year\'s flu virus according to the Chinese calendar?'
)
en[405021] = (
'lehkor_: hehe. waited long for a used 3750 kitty, the dealers assured the hardware was coming from Japan.\n'
'lehkor_: it arrived. inside is a config of Moscow corbina'
)
en[405022] = (
'IceBerg: Everyone tells me I was born for radio )\n'
'magenta: They\'re tactfully hinting that your face didn\'t make it for TV :-D'
)
en[405023] = (
'xxx: You\'re 19, and you don\'t know such simple things as css. I\'m ashamed of you =)\n'
'yyy: I\'m a girl\n'
'xxx: So what? Working with css tables doesn\'t require a dick.'
)
en[405024] = (
'Dakaskos: Every person should have an antipode - their complete opposite. Somewhere in the world there\'s a sad rude guy who has a girlfriend and money. What a dick... ('
)
en[405025] = (
'xxx: How\'s it there? Wife-kids?\n'
'yyy: the kid\'s growing ) \n'
'yyy: the wife too ('
)
en[405026] = (
'shaixulud: Someone in user Vasya\'s directory changed .python-eggs\n'
'shaixulud: I walk up to the admin now and ask, like, webdev\'s glitching, did you touch Vasya\'s eggs?\n'
'shaixulud: The admin was completely knocked out :) Then he ran around asking everyone who touched Vasya\'s eggs :)'
)
en[405027] = (
'Depilator reviews:\n'
'I don\'t know about removing hair, but it plucks chicken carcasses well. My husband gave it to me for my birthday. We live in a village, so it\'s a very needed thing in the household!'
)
en[405028] = (
'olegart\n'
'Logically, it was reasonable to assume that in a wall made of sawdust concrete the rebar would be wooden.\n'
'But still SUDDENLY.\n'
'\n'
'mutoids\n'
'There was a case when drilling a LOAD-BEARING wall, the drill wrapped pieces of a quilted jacket around itself. Someone once made a joke.\n'
'\n'
'knazz\n'
'They simply walled in the foreman. To make it stand stronger.'
)
en[405029] = (
'xxx: You made two mistakes in one word )\n'
'yyy: Fool, that\'s how it\'s done on the internet!!!!!'
)
en[405030] = (
'Mary: \n'
'Sashka! I decided to change jobs, ran into a problem - I have no education and specialization, and I can do anything...\n'
'\n'
'Mary: \n'
'And how do I tell about that in a resume?\n'
'\n'
'Alex: \n'
'well, basically simple\n'
'resume:\n'
'"Can do anything. Marina.\n'
'tel.XXX-XXX-XX-XX"'
)
en[405031] = (
'She: Too late, dear, too late. A word is not a sparrow, if it flies out - you won\'t catch it )\n'
'He: Oh come on! My words are like penguins. If they drop - they won\'t waddle far!'
)
en[405032] = (
'xxx: what can a dumb creature be thinking about, taping up with newspaper all the ventilation holes on the system unit?\n'
'xxx: of course, that less air will blow on its legs now!\n'
'xxx: I\'d kill!!!'
)
en[405033] = (
'Valera: Eh, if I could, instead of walking for the kettle, declare an object of type kettle and link it to the kitchen object, casting it to type kettle\n'
'Max: If you could just declare a kettle object on an empty spot like that, you\'d be a great mage and sorcerer. Things like a kettle have private constructors!\n'
'Valera: :D Exactly! Imagine how much of a pain it is to be an assembler sorcerer!\n'
'Max: And by the way all sorcerers in fairy tales are assembler sorcerers. "Sim salabim, trach tibidoch", nowhere was there new kettle(with handle, with crooked spout)!'
)
en[405034] = (
'xxx: does your cat scratch furniture?\n'
'yyy: Now we have no furniture! Only scratching posts! Big scratching posts, small scratching posts, lying scratching posts, humanoid scratching posts...'
)
en[405035] = (
'xxx: better find a girlfriend and devote to her as much free time as you\'re going to sink into the internet :)\n'
'yyy: where will I find a girlfriend for 900 rubles a month? And unlimited at that.'
)
en[405036] = (
'xxx: do you have vaseline?\n'
'yyy: for work?'
)
en[405037] = (
'xxx: how\'s your kitty doing?\n'
'yyy: Like an evil spirit )\n'
'yyy: It only crawls out from under the couch after sunset!'
)
en[405038] = (
'xxx: And what did you feel after you broke up with her?\n'
'yyy: Like I lost 10 rubles. Kind of a pity, but whatever...'
)
en[405039] = (
'ArchAngel:\n'
'At our university entrance there\'s a barrier gate. So, it sometimes glitches, and it closes right on a car passing under it. So our smart guys instead of fixing the barrier gate, wrapped it in FOAM RUBBER, so it wouldn\'t scratch cars.'
)
en[405040] = (
'vedma_mashka: when there are many guests and few chairs, you can lay a snowboard on two system units - you get a bench.'
)
en[405041] = (
'lozhka:\n'
'till what age can you allow a 15-16 year old girl to walk outside?\n'
'\n'
'DezhurnayaPoOtdeleniyu:\n'
'I think, till about twenty.\n'
'Then - married.'
)
en[405042] = (
'xxx: Found a hundred bucks in a book about Buddha. DearLordyGod, that\'s how you work with potential clients.'
)
en[405043] = (
'Ivan: Yeah, I have experience as a soundman, worked with various equipment, at many venues of our city!\n'
'Dashka: Vanka, what\'s with you? You\'re 12!\n'
'Ivan: Oops, not for you...'
)
en[405044] = (
'Discussion of an accident between a Peugeot and a VAZ:\n'
'xxx: And doesn\'t a VAZ have airbags?\n'
'YYY: It does, the driver apparently didn\'t have time to inflate them'
)
en[405045] = (
'RCX &#8206;(22:56): \n'
'Doing homework on probability theory, task:\n'
'"In a box 15 parts, of which 5 are defective(33% defective!). An assembler randomly extracts 3 parts..."\n'
'Damn, how painfully familiar, that\'s how AvtoVAZ assembles cars, it\'s only missing the phrase "and with closed eyes screws them into a randomly chosen place"\n'
'\n'
'Rain (23:04):\n'
'it\'s bad to laugh at the unfortunate...'
)
en[405046] = (
'ksanac:\n'
'This morning I had the apotheosis of undersleep - I poured the cat unpeeled pine nuts instead of dry cat food. And we looked into each other\'s eyes for a veeery long time...'
)
en[405047] = (
'The kid just delighted:\n'
'N: Go away, fly, go away!\n'
'Me: A fly has no ears - it can\'t hear you.\n'
'N: But it has legs!'
)
en[405048] = (
'Jurgen:\n'
'"Chinese scientists from the Paleontology Institute of Peking University led by professor Changzhu Jin came to the conclusion that homo sapiens originated not from Africa, but from China."\n'
'\n'
'Gromov:\n'
'I think our Ukrainian comrades will refute them'
)
en[405049] = (
'xxx: I need speakers for an old turntable, I know you have some.\n'
'yyy: Have some, in the basement.\n'
'xxx: I really need them, pancakes with condensed milk on me.\n'
'yyy: Gotta take a flashlight, a rifle, bandages, iodine, an inflatable woman and go. (\n'
'xxx: Errr... the flashlight is clear why, let\'s go in order, why the rifle?\n'
'yyy: I haven\'t been down there in three years, who knows what settled there.\n'
'xxx: And the bandages and iodine in case this "someone" turns out faster and stronger, right?\n'
'yyy: No. In case I shoot a neighbor in a scare.\n'
'xxx: Well and why the woman?\n'
'yyy: Just kind of scared alone.'
)
en[405050] = (
'xxx: what did you do today, darling?\n'
'yyy: went to get a certificate for uni, then dropped by my parents\', gave dad that disc I told you about, then to work.\n'
'xxx: and I thought you missed me :(\n'
'yyy: sorry, I keep forgetting the right answer'
)
en[405051] = (
'Hiro:\n'
'The truest sign of truth is simplicity and clarity. Lies are always complex, elaborate and verbose. /L.Tolstoy/\n'
'\n'
'-Cheshirec-:\n'
'and that was said by the man who wrote "War and Peace"'
)
en[405052] = (
'From a forum:\n'
'When a person starts mastering Photoshop, they spare no one - neither family, nor friends, nor pets...'
)
en[405053] = (
'mozg: at today\'s creative meeting they told me I\'d make an excellent necrophiliac gravedigger at the cemetery of ideas... Tell me, what did they mean?'
)
en[405054] = (
'DenFishera: Today I fed birds and salad a whole cutlet in total. And they shit on my leg\n'
'DenFishera: Probably humans evolved from birds'
)
en[405055] = (
'xxx: Do you have skype with a mic? The comp is dying.\n'
'yyy: Should I eulogize it?'
)
en[405056] = (
'xxx: AND WHAT ABOUT THE PASSWORD? REMIND ME, IT SAYS - INVALID\n'
'xxx: blech\n'
'xxx: got it'
)
en[405057] = (
'from twitter\n'
'Our cafeteria is like a torrent tracker. Every now and then the cashier yells "Somebody seed already!"'
)
en[405058] = (
'daradali:\n'
'After thoughtful hissing of the film the speaker announced: "...Hell. Next station - Black".\n'
'Gothic Petersburg metro.'
)
en[405059] = (
'<ForNeVeR> The switches to daylight saving and winter time especially deliver. Imagine, for example, a police report telling about events at 3 am on the last Sunday of October...\n'
'<ForNeVeR> 2:59 - the accused came to the victim\n'
'2:25 - the victim is killed\n'
'<catap> ForNeVeR: nice!\n'
'<ForNeVeR> And that\'s it, 100% alibi.\n'
'* ForNeVeR wrote down in his notepad: "kill only on the last Sunday of October"'
)
en[405060] = (
'A buddy decided to officially register his small fountain production in garage conditions. Thought long about a name, finally came and says:\n'
'\n'
'He: (With a serious look) Came up with it! "Sharkon".\n'
'Me: Probably something French? And how does it translate?\n'
'He: Sharashka\'s Office :D'
)
en[405061] = (
'kwel: "Zubaha stated that "the company has its own information technology department with 200 people working in it. And it is able to implement the lion\'s share of the project work on its own""\n'
'kwel: I always liked the expression "lion\'s share of work".\n'
'kwel: Considering that in a pride the lion\'s part of the work is to eat what the lionesses brought and then screw all of them.\n'
'kwel: Basically the guy wrote everything right, but how he disguised it...'
)
en[405062] = (
'X:\n'
'calling my husband today to wish him a happy Friday the 13th, and he says he\'s handing over a site and the accepting guy\'s surname is "Mogilny" (graves-ish), well I clammed up and wished him luck'
)
en[405063] = (
'******** (13:13:13 13/11/2009)\n'
'Vitaly, no salary today\n'
'\n'
'Vitaly (13:15:54 13/11/2009):\n'
'And don\'t believe in mysticism after this :('
)
en[405064] = (
'mur_myau: The cat invented a new pastime. Touches one of the edge bottles on the shelf with her paw and watches the "domino effect" with interest. >_<'
)
en[405065] = (
'xxx: I shake hands only with those who didn\'t ask me to pump porn'
)
en[405066] = (
'Leks: a morning is good only when it\'s morning in the afternoon.'
)
en[405067] = (
'xxx: Anya. And what do you know about him?\n'
'xxx: Besides that his name is Stas, he studies at NPI and lives on Galinka\n'
'yyy: well, I usually exchange photos with everyone I meet\n'
'xxx: ah, well that\'s quite prudent\n'
'xxx: go to the police and look for him on the "wanted by police" board'
)
en[405068] = (
'xxx:\n'
'you gotta work yourself to death editing a math textbook to read "Dispersion" as your status instead of "Depression".\n'
'xxx:\n'
'- why are you in dispersion today?\n'
'- ah, I have such a standard deviation today...'
)
en[405069] = (
'K: Hi\n'
'A: hi\n'
'K: Got a matter\n'
'A: ?\n'
'K: More precisely, two\n'
'A: ??'
)
en[405070] = (
'xxx: Today\'s Friday the 13th )\n'
'yyy: And what\'s with the thirteenth? Today\'s FRIDAY!!!!!'
)

expected_ids = list(range(404971, 405071))
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
