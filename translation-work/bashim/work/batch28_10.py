#!/usr/bin/env python3
"""Append English translations for bash.im chunk 28, records 901-1000."""
import json, os

BASE = '/home/hatch/workspace/translation-work/bashim'
srcp = os.path.join(BASE, 'chunks/chunk_28.jsonl')
outp = os.path.join(BASE, 'out/chunk_28.jsonl')

EN = {
410979: """ххх: came from the army. went into VK
ххх: look at my status - "I'll dodge the army, be right back :)"
ххх: Updated 365 days ago.""",
410980: """ххх: yeah. and then Indians will run in and take your Skype off!
ххх: oops! scalp*""",
410981: """after watching a series in which one of the heroes after an accident can't remember the last year of his life, I thought with horror: what a terrible thing, such amnesia - and you remember yourself, and friends and relatives help as they can, but the passwords, THE PASSWORDS! how to restore them?""",
410982: """xxx: you can't be trusted with children, like I can't be trusted with lunch
yyy: think I'll burn it after all?""",
410983: """xxx: Hello everyone. Need to promote an auto parts site in search engines. Any SEO specialists here?
yyy: They all left to prepare for the algebra test :(""",
410984: """sdirit: never had a high opinion of the mental development of those who write in elevators, but the inscription "whoever wrote this will get fucked up" made me think...""",
410985: """GOLD: Read that a new version of the Jaguar Gold cocktail started selling. Who knows where it's sold in Tula?
Tensi: household goods, the "household chemicals" department""",
410986: """xxx: I'm walking through the cemetery, carrying flowers.
xxx: I see: something is digging out from under a grave.
xxx: then something pink climbs out...
xxx: a mole, fuck (""",
410987: """Andronix: recently saw new flash drives shaped like rectal suppositories...
Jax: what's the joke?
Andronix: the seller explained that in case of a mask-show they hide easily and quickly.""",
410988: """xxx: What is recursion?
yyy: Self-pickup of a procedure or function.
yyy: Self-call""",
410989: """Discussion of the news "In Japan invented a device for imitating kisses":

...As reported, with the help of this device two internet users who are at a distance from each other can "kiss"...

ххх: They'd better invent a device that can punch in the face through the internet. That would be a very relevant thing.""",
410990: """A friend (F) is given an apartment, like, live on your own, daughter.

F: Only I don't know if I can live on 20 grand a month.
Me: In principle yes, but won't your mom pay utilities?
F: No, mom will pay for food, bills and clothes, but 20 grand for going out...

That's how class hatred awakens in students.""",
410991: """Lexw said:
Didn't understand. I want a regular city number. But over IP telephony. And I ask which of the Tyumen providers can provide this. You're offering me to become a provider, buy licenses, number capacity and thus get what I want? Are you by any chance a gentoo user? ))))""",
410992: """XXX: a project is an ass, and the client's requirements are a window, and often we need to shove this ass into this window
Белая рысь: Actually, to be precise, you need to stuff the window into the ass, because the project must contain all the requirements, not the other way around.""",
410993: """From an explanatory note "......I fell and inflicted an injury on myself through carelessness of the brain\"""",
410994: """ххх: Yesterday I was catching the bride's bouquet. )
ххх: Caught it, but then gave it to another girl who had nothing working out all day. Took just one rose for myself. )
ууу: Right, so that the bouquet has an even number of flowers left.
ууу: did her a favor. xD""",
410995: """xxx: sat at the computer, put potatoes with dill in front of me, herring next to it, bread, beans in tomato sauce, tea, put gingerbreads and then on the wall fifteen centimeters from you a huge fat cockroach crawls out.
xxx: after that you don't even want to kill it. you want to smash it.""",
410996: """ххх: A 5-year-old niece came to visit, brought a rat that's been living with her for a couple weeks. The rat is big, well-fed, all white, with a gray stripe on its back.

Я: What a funny rat Masha has, and such an unusual coloring, gray undercoat on the back.
Мама: Yeah, the fur on its back used to be black.
Я: Why is it gray now?
Мама: Life is hard for her.""",
410997: """xxx: do you think, in May it gets warm enough to sunbathe?
yyy: it happens, of course. especially often this happens with the weather in those years when you need to enter uni (school graduation + preparing for entrance exams) or write a diploma.""",
410998: """worms: want food, sleep and a girl!
worms: who will cook food, put me to sleep and will level my char
Diesel: *facepalm*
worms: think you can't trust girls with a char?""",
410999: """XXX 10:57: when trying to create the same folder it says such a folder exists""",
411000: """<sh> Why can't you backup an apartment before a birthday? =(""",
411001: """xxxx(18:00:02): I'm tired of something. Let's kill the server already :)
yyyy(18:00:34): told people at seven.
xxxx(18.01.22): want me to switch your main NTP server? Everyone will have seven :)""",
411002: """xxx: I'm running out of rags I should keep quiet into""",
411003: """From a women's forum in a blowjob thread:

> yes ) I love it too ))
> my bunny first didn't want to, refused, like I'm a girl and he was embarrassed that I do this

Uh, and who did it for him before?""",
411004: """ххх: got up in the morning, did a daily
ууу: for me getting up in the morning is already a daily""",
411005: """xxx: And yet Americans are a happy people.
xxx: Invented themselves, raised themselves, grew themselves, chased themselves, killed themselves and rejoice themselves...""",
411006: """ххх: And what about "a woman loves with her ears"?
ууу: In the internet era, a woman loves with her eyes.""",
411007: """xxx: Mother-in-law arrived, rearranged everything in the kitchen, changed the furniture in rooms.
xxx: How do I explain to her that you don't touch shortcuts on someone else's computer desktop?""",
411008: """ххх: do you know what remote administration is?
ууу: roughly, why?
ххх: remote administration is when the secretary comes and says: "I'll send a paper to print now, when it comes out, I'll shout to you, you'll flip it and put it back in the printer" :)""",
411009: """ххх: here's what I found: ""Wingless Gosling" - a psychedelic cartoon filmed based on a Chukchi fairy tale. There a person constantly tears off his head so birds fly out of his torso. And the poor gosling is either buried alive by a fox, or lowered into hell by mice turning into cockroaches... In general, in 10 minutes of cartoon the Sverdlovsk film studio could easily make a neurotic out of a child."
ххх: I think it's worth watching""",
411010: """I work as warehouse manager, we have two warehouse rooms, but the phone is only in one. While I was driving to the far warehouse, a manager from the office called. I arrived, the guys tell me: Polina called you, urgently wanted something. I call.
- Polina, did you want something?
- No, you took so long to call back that I read your letter to the end and everything was written there. And she hung up.""",
411011: """In a store choosing a player. Consultant (k) and me.
K: Hello, by what characteristics would you like to choose a player?
Me: Well I'd like it to be louder...
K: Volume is not the main thing, by what characteristics would you like to choose a player?""",
411012: """Gregory: It's called "preliminary likes", when you like before someone commented.""",
411013: """xxx: My husband calls: "look from the balcony"!
xxx: I think he wrote "I love you" on the asphalt.
xxx: And he - "see where I'm standing, our wifi catches here".
xxx: Romance! )""",
411014: """Vacancy with hidden meaning: "Drivers with personal cars required, for transport service of organizations\"""",
411015: """Selena: I almost lost my mind on May 1st. Couldn't sleep in the morning, and the little one got up. Dacha and all that. So we went with her to cut branches for the parrots and dig up dandelions, so as not to wake Kostyan. We're digging, and past us on ATVs men ride back and forth, rode past three times. I thought people just hadn't gone to bed yet, it's 8 AM. And then they stop near us, and in their hands a gun and a huge net... I got scared, hid the little one behind me, gripped the hoe tighter. And the men plaintively: Girl, our peacock chick ran off, have you seen it?
I was dumbfounded, honestly, and automatically: What peacock chick?
They: Fat, can fly, female....
Turns out these are zootechnicians from the nearest resort who got drunk the night before and their peacock escaped.""",
411016: """arxont: And in general. The server room should look so that you're not ashamed to invite another admin.""",
411017: """xxx: what is CTO?
yyy: well, and you're an IT guy, - it's Chief Technology Officer
xxx: You see, I'm a simple software developer and therefore by nature of service I communicate at most with Chief Technology Praporshchik""",
411018: """xxx: I have a simple rule: if a downpour outside caught me at home - don't wait for me at work. If the downpour caught me at work - don't wait for me at home (""",
411019: """lll: Frogs can't effectively chew food and use their tongue to push it into the throat. Instead they push food with their eyes, drawing them into the skull with special muscles — that's why frogs often blink while eating.
222: I'm a FROG""",
411020: """ХХХ: and tomorrow I'll finally go treat my teeth for the first time (last time it fell through). so my day won't be so cool

YYY: No worries, now there's good euthanasia
YYY: anesthesia I mean""",
411021: """xxx: watching internal cameras from my old job, which broadcast online
xxx: constantly either no one, or beer bottles and pizza boxes in the trash
xxx: work is clearly boiling""",
411022: """xxx: Downloaded a pirated book in pdf that was protected by a password.
xxx: Downloaded a password cracking program.
xxx: Downloaded a crack for cracking the password cracking program.
xxx: Now fighting the feeling of my deep immorality...""",
411023: """xxx: If you noticed your friend painting his nails with polish, what would your reaction be?
yyy: lightning-fast!""",
411024: """uu: I'm tired morally
maxwebz: I'm tired amorally
uu: amorally? what's that like?
maxwebz: fucked up!""",
411025: """snorapp:
Still, when I see in Facebook automatic messages like "Petr Obolonsk��mu and Matvey Lensky like the link", I involuntarily flinch: like, compared to hard labor?...""",
411026: """xxx: I'm lazy, greedy, conflict-prone and don't like people.
xxx: Writing a resume and realize I'm also a liar.""",
411027: """xxx: We have a cat at home.
xxx: Named Samael Vincent van Gogh.
xxx: Because he managed to gnaw off his own ear...""",
411028: """ччч: on the first floor in the dorm there's an announcement:
on such-and-such date for subbotnik 4 boys and 2 girls are needed

ччч: all that's left is to add - and a video camera)""",
411029: """xxx:
"A serious successful company needs a driver with a representative-class car".
Makes you want to add "...and a hole puncher\".""",
411030: """mvsh: Forgot to turn off the dashcam in the parking lot. Now I have 16 gigs of awesome night rain!""",
411031: """ххх: Put on a wedding ring. Got +100 to opposite-sex attention. Where's the logic?""",
411032: """xxx: realized what a nerd I am when my parrot started screaming in a Call of Duty 4 voice "Our UAV reconnaissance is in the air!\"""",
411033: """xxx: was going to the kitchen to drink water. there were no clean glasses, decided to wash one. got lost in thought. when I came to, I see - I've already washed all the dishes. and mom always warned me: "yulya, don't fly in the clouds\"""",
411034: """ххх: and have you never thought that suddenly we are all NPCs in some future computer game with full freedom of action, and therefore the whole world is computed? That someone is doing a mission, say, kill the terrorist Ben Laden, and then will turn off the computer (in 2012, say). And what will happen then? And in general, why do anything then?
ууу: completely lost it. study calculus, tomorrow's a module, the teacher doesn't give a fuck that he's an NPC.""",
411035: """Art: Neighbors listen to loud music and have sex, so it's audible. And I'm writing a term paper.
Feel old at 23.""",
411036: """black_dez: Was returning home. In front of the house stands a six (Lada). On the rear window a sheet of paper is glued, and on it the inscription: "The car is clearly visible from the window. I shoot without warning!\"""",
411037: """Hello!

Please replace my Benq FP73G monitor, due to its malfunction.
04.05.2011 having turned on the computer, the monitor crackled and a burning smell started from it.
Temporarily took a monitor from a neighboring computer to write this request.
If possible please provide a 19" monitor, because the resolution of a 17-inch monitor is insufficient for performing work duties (For example website layout, developed for 19-inch monitors).""",
411038: """comments to an article about stripes on a zebra's body:
ххх: zebras, as is known, graze where it's hot, very hot. the black stripe heats up more than the white. the air above the black is of higher temperature than above the white. at the junction of "airs" of different temperatures, vortices form. these vortices cool the zebra
ууу: In especially hot weather the vortices even lift some zebras into the air.""",
411039: """xxx:
When we were students we went through a medical exam.
We sat in a flock of girls, chirping before the neurologist's office.
A middle-aged male doctor came out, looked everyone over, chose two - me and my friend.
In the office he told us to undress above the waist, stretch arms forward, close eyes and stick out tongue.
Quite a picture, I understand.
Apparently, when the tongue is hanging out and eyes closed, and hands are shaking, time stretches very long.
We'd already started swaying and toppling to different sides when he said: "You can get dressed".
AND HE JUST LET THE REST IN AS A GROUP AND STAMPED THEIR CERTIFICATES.""",
411040: """xxx: so how's the diploma?
xxx: what diploma, everyone's solving a much more important problem here: how much vodka to buy for graduation...""",
411041: """spurs: friend (f) calls payment system tech support (t)
t: what is your problem?
f: it's YOUR problem, I deposited money a week ago, still hasn't arrived
t: sorry, lately the number of clients has increased, and the data didn't process, so we're crediting payments selectively...
f: select me!""",
411042: """Kisa Vorobianinov: Sometimes in automated systems business logic is implemented in such a way that blaming women afterward for incoherence and inconsistency is a crime""",
411043: """xxx: I think the people who decide where outlets go in a house bring a carousel with them, ride it for a long time and then randomly poke fingers along the walls.""",
411044: """xxx: sometimes I think I'm the place under that famous rock in Sparta. All the ugliest and sickest boys are thrown down to me..""",
411045: """xxx: and now, five years after defending my diploma, calculus finally came in handy: I solve differential equations for my sister, and she washes dishes for me...""",
411046: """<Melamory> why is everyone silent here?
<[CiF]SlayeR> Melamory this is a special kind of chat
<[CiF]SlayeR> chat - silent
<[CiF]SlayeR> develops telepathic abilities""",
411047: """9-11: Other kids in stores beg "Buy the cartoon", but my well-behaved one runs across the whole supermarket with a disk and the loud question "Dad, will you DOWNLOAD this cartoon for me today?!" """,
411048: """xxx: Headlights with the inscription "CDЕПАНО В РОССNN" make me doubt their place of production""",
411049: """Дима:
he sets tasks poorly

freya:
yeah. like: you need to hammer a nail. you immediately rejoice - oh like, no big deal. just hammer a nail. he continues: into a fence. that we're now going to build. around a house. that doesn't exist yet. and you'll need to start with cutting down the forest where the house will be.""",
411050: """xxx: Antoshka-Antoshka, let's go mow the dough...
Антон: Tili-tili, trali-vali, I'm a lazy shopessets, can't be bothered to find a rhyme either.""",
411051: """xxx: "Obama decided not to publish photos of killed bin Laden"
yyy: schoolkids have never been deprived of so many demotivators.""",
411052: """interfax.ru:
The presidential property manager Vladimir Kozhin told about the hard lot of officials. The "sweet life" of civil servants is fairy tales, they "have to pay for everything", claims V. Kozhin, who earned 17 million rubles in 2009""",
411053: """From a legal advice site:
I sold my dog. Then I changed my mind. And demanded it back. Money of course returned. But a small trouble happened. I don't remember who I sold the dog to and who I returned the money to. What should I do?""",
411054: """yyy: I've been ringing you all day! why aren't you calling back?
xxx: I have pride!
xxx: and no money :(""",
411055: """Adam: EVERYOOONE I'M GOOOING TO GET REAAADY
Глеб: go-go, take your laptop with you
Adam: GOOOT IIIIT ALRIIIGHT
Adam: THE SMAAALL ONE OOR THE BIIIG ONE
Глеб: take both! I'll rip your CAPS off on both!""",
411056: """from mail answers

Евгений %№"№%:ий
please tell me online pregnancy tests without scams""",
411057: """Comment to news about the script of the fifth Pirates:

The script of the fifth "Pirates" is ready, but Johnny Depp asks to return his passport""",
411058: """sms from her: I want burgundy silk bedding!)
me: with guitarists, tents and a campfire?""",
411059: """From L2 chat dialogue
xxx - tomorrow's a siege, the grands will come and we'll lie down (
yyy - Don't be afraid, I took measures
yyy - Dropped their clan leader a link to Code Geass, so in the next two-three days he won't log into the game.""",
411060: """ххх: and at my former job a man came up to the display case with flash drives and looked at them for 10 minutes. Looked and looked, then says: "Your lighters are kind of expensive\".""",
411061: """xxx: Torrents were invented by Jesus Christ — he bought 5 loaves and 2 fish, and then distributed them to 5000 people.""",
411062: """Andrey_Ekb: The other day was buying shawarma at a kiosk. A local mountain child, thoughtfully looking at the shawarma and the cellophane bag, delivers an epic phrase: "For you right away or now?"
Andrey_Ekb: I even got thoughtful.""",
411063: """Flying with my wife in a plane, she's by the window.
W: It's gonna shake now.
Me: Did you see air pockets there?""",
411064: """xxx: As a kid I could never understand why Super Mario can break a brick with his head, but dies from touching a turtle""",
411065: """Grishka: The guard made my day...
Grishka: I go out between requests to smoke. I come back. He out loud: Best smoke break - 2 minutes 14 seconds! He must be bored.""",
411066: """ZZZ: they advise me here to be a dumb and weak woman. Fuck, I changed the siphon under the bathtub, I understand plumbing better than the plumbers themselves, I move furniture and set up equipment! And they advise me to pick up the neighbor asking to help replace a light bulb!""",
411067: """Андрей Владимирович: left spaghetti in a colander overnight
Андрей Владимирович: in the morning shook it out - got a brain model
Андрей Владимирович: feel like Cthulhu!""",
411068: """xxx: I so wanted to hear his opinion about me
xxx: I already imagined how he, smiling, says
xxx: "She's ideal. I'll tear anyone apart for her. She knows 3 languages, draws beautifully, a beauty and a smart girl".
xxx: But when he was asked if he has a girlfriend, he, chewing something, muttered
xxx: "Yeah, Katka. She's a gopnik"
xxx: ...""",
411069: """xxx: I kiss you on the neck, then on the collarbone... want me to go down?
yyy: oh yes..
xxx: I'm going lower and lower..
yyy: mmm..
xxx: and now I'm on the ground floor!""",
411070: """From the feed, discussion of the movie Transformers-3:

Dispenser
I think or are the movies getting dumber and dumber, and the chicks scarier and scarier?

Epipremnum aureum
no, it's you with time becoming wiser and wiser, and the testosterone level in blood lower and lower""",
411071: """Letter to tech support: "I want to delete myself from the site, since my ex-husband found out about it. He printed my photos and mocks them\"""",
411072: """nikkass1989: How to get a girl to like you?
awc_nick: Better face-first, but it's a matter of taste""",
411073: """xxx: hate food whose smell is tastier than its taste""",
411074: """xxx: Explain, why tie St. George ribbons on a car and drive, without looking back, past a veteran with medals and orders hitchhiking on the road?
xxx: Are we proud? Do we remember?""",
411075: """from a forum:
xxx: I figured out how to make my husband shave: in the morning, while he's sleeping, I quickly smear foam on one cheek and shave. He wakes up, but it's too late, he goes and shaves the rest himself )""",
411076: """Оксана: how do we find a good dentist here?
Саня: by trial and error, but you only have 32 attempts.""",
411077: """From Habr. GMail increased the contacts limit in the address book from 10 to 25 thousand.

First comment:
ххх: Oh, great news, long waited, now it's much more convenient!""",
411078: """xxx: and after the first orgasm I stopped understanding what's offensive about being told to go fuck yourself""",
}

def main():
    recs = {}
    with open(srcp, encoding='utf-8') as f:
        for line in f:
            line = line.strip()
            if line:
                r = json.loads(line)
                recs[r['id']] = r['text']
    assert len(recs) == 1000, len(recs)
    for k in EN:
        assert k in recs, k
    cur = 0
    have = set()
    if os.path.exists(outp):
        with open(outp, encoding='utf-8') as f:
            for line in f:
                line = line.strip()
                if line:
                    cur += 1
                    have.add(json.loads(line)['id'])
    with open(outp, 'a', encoding='utf-8') as f:
        n = 0
        for i in sorted(EN):
            if i in have:
                continue
            f.write(json.dumps({'id': i, 'ru': recs[i], 'en': EN[i]}, ensure_ascii=False) + '\n')
            n += 1
    print('batch28_10 OK, wrote', n, '(output now', cur + n, 'lines)')

if __name__ == '__main__':
    main()
