#!/usr/bin/env python3
"""Translate bashim chunk_28 records 1-100 (input lines 0-99). ru is read verbatim
from the input file; only en translations are embedded below."""
import json

EN = {
410078: """A call from the client's rep, her voice nervous, agitated:
- Excuse me, are you going to work on our project or what?
Not quite understanding what prompted the call, I answer, confused:
- Yes, of course, it's in the works.
The client woman continues, annoyed:
- But you promised to deliver it on Monday???!!!
Now I'm completely lost:
- Yes, and so?
Judging by her voice, I just got checkmated:
- Today is THURSDAY!!!!
Still trying to make sense of what's happening:
- But you only ordered it from me the day before yesterday, on Tuesday????
- Really?? Oh, sorry...""",
410079: """xxx: My mom scares me.
xxx: She bought some new tea, I'm sitting at my computer drinking it.
xxx: She comes over and says: "Smell this — what does the tea smell like?" Me (used to her always brewing it strong): "Strong tea?" Mom (cheerfully): "No, potassium cyanide!"
xxx: I choked!
xxx: Turns out it was almond tea...""",
410080: """Мама: Sweetheart, why did you come home today without the smell of booze on you? Did you have a fight with the girls?!""",
410081: """xxx: The most absurd thing I've ever seen is a person walking down the street during a wild freeze, slipping on a banana peel.""",
410082: """xxx: I come up to you from behind, hug you, my hands on your belly, they slide lower and lower, and your belly just never endsa...""",
410083: """xxx: You are an ostrich. You stuck your head in the sand and hid from your problems!
yyy: I didn't hide, I'm eating the mole.""",
410084: """ДК: I had a neighbor in the communal apartment who was impossible to wake up
ДК: absolutely impossible, if he was asleep
ДК: once he was asked to come to the phone, during the day, he was asleep
ДК: I went to wake him.
ДК: I kicked him, screamed, waved the alarm clock — nothing helped
ДК: then I poured water on him from the kettle
ДК: he opened his eyes, looked and asked
ДК: what, are you fucking nuts?
ДК: turned over and fell back asleep""",
410085: """My parents went to student construction brigades in their youth. And so, in Vladivostok they happened to chat with a saleswoman in a bookstore. For the record, books were hard to come by in our city at the time. So there they stand, a bunch of students, looking around, admiring... And then they see a volume of Yesenin. Then the dialogue:

Папа: Wow! You've got Yesenin too?!
Продавщица: Of course! Interested? I love him too! Although I've been disappointed in him for a while now...
Папа: Why is that? (naturally everyone pricked up their ears, an interesting debate is brewing!)
Продавщица: He hasn't written anything new in 20 years!""",
410086: """Doctors' forum:
Today just a wonderful call: "... And so I was treated by one doctor — he didn't help at all. He prescribed some pills, and they didn't help at all. True, I didn't take them, I was afraid, but the doctor didn't help at all."
(This is a verbatim transcript)""",
410087: """xxx: a call to the call center: "help!!! the ATM is strangling grandma!".
xxx: turns out granny punched a hole in her card and hung it around her neck so she wouldn't
lose it, she put the card into the ATM, must have typed something wrong.
xxx: then the ATM grabbed the card into the reject cassette, and grandma got pulled in along with it....
xxx: ATMs are killers!""",
410088: """OutGreeNыЧ: I come to work in the morning, and there's a ruckus. The toilet burst and flooded the 1st floor, which is where the bacteriology lab is
OutGreeNыЧ: they've never received so many samples at once""",
410089: """xxx: today the moon will come as close to the earth as it gets. do you know what that means?
уyy: tickets to the moon will get cheaper?""",
410090: """x: What is a mandrake root?
y: A mandrake root is an entity which, when squared, yields a mandrake.""",
410091: """Олег: I knew my signature wasn't complicated, but when the OCR recognized it as text, and even recognized every letter, I got seriously worried.""",
410092: """dema: The brass are calling — a solid guy, gray-haired, around 50, serious, no compromising associations noticed
dema: And a kid is getting underfoot
dema: The boss asks who's barging into the conversation
dema: Against the background of a second's pause, a child's voice sounds — Uncle! Let's go live in the teremok!
dema: An instant reply from the receiver — What?! The mouse-norushka, the frog-kvakushka, the bunny-popygayka, the fox-sister, the wolf-gray-flank, and the bear-toptyzhka are already there! I won't fit in, I'm fat!!
dema: And in a different tone, now to me
dema: I'm expecting the corrected text exactly at 5:00 PM, because at 5:30 it needs to be shown to the client!
dema: The kid then threw a tantrum for an hour demanding to be taken to work and urgently befriended with such a kind uncle""",
410093: """xxx: Decided to boil pelmeni for lunch.
xxx: To add some aroma I thought of adding a bay leaf, searched the shelf and found some dried leaves, decided that's what I need, tossed them in the pot.
xxx: The pelmeni ended up tasting like eucalyptus.
xxx: Fucking koala.""",
410094: """xxx: I've gotten mean lately. I carry brass knuckles like a teenager carries a condom — just in case the opportunity comes up.""",
410095: """он: What time will you be done cooking in the kitchen?
она: Don't know.
она: It's my first time.""",
410096: """xxx: A couple of weeks ago my husband, getting ready to view apartments, met with an agent — a young woman. They got in her car and drove to the addresses. It was early Saturday morning, my husband was suffering a hangover, and even more so — the shame that his breath was quickly fogging up the car's windows.
xxx: But the girl wasn't doing any better — on the fogged-up rear window of the car, the imprints of women's feet were clearly visible.
xxx: So they rode along, blushing and keeping quiet, the whole way.""",
410097: """ЕС: It's spring, and my Lyokha just sits at home, never goes out.
ОЕ: Doesn't he work for you, Yevgeny Stepanovich?
ЕС: Him? No. Why would he? LOSERS work, Oksana Yevgenyevna. Like you and me. And he's in show business — a white stripe, a black stripe.
ОЕ: But doesn't he live with you? He said he doesn't live with his parents anymore?
ЕС: That's right, he doesn't live with us. He lives separately. But he EATS, SLEEPS, AND SHITS WITH US!!!""",
410098: """neustup: and my granny still remembers cordless irons...""",
410099: """From a dialogue with a biker who was recently dumped by his girlfriend:

Ада: how's it going
Хмырь: fiiiine!!!
Ада: did she come back?
Хмырь: Nope, I bought LED turn signals and a turn-signal relay to go with them""",
410100: """xxx: No risk, no Postinor!""",
410101: """a forum is discussing a crash caused by road conditions:

ххх: the road there is certainly shit, but to be fair, at what speed did you fly into the pothole? honestly. there are warning potholes there. you can slow down in time from sixty.

yyy: a new trick from the road workers — "warning potholes"

zzz: before the "warning potholes" comes the so-called "alarm road surface\"""",
410102: """ххх: A cultured person would never say "Shove your opinion up your ass!". He would say: "Clothe your judgments in softer forms!\"""",
410103: """xxx:
Everyone urgently needs to get a parrot.
A minute or two before an earthquake they scream like lunatics and thrash around the cage.
And put an emergency go-bag by the door. The parrot screamed — grab the bag and get out.
yyy:
thanks, had a laugh.
do you know how many false alarms they have a day?""",
410104: """from a forum on Celts:
I wouldn't call Munro 100% fantasy. A lot of what he writes is based on real documents. But you shouldn't fully trust a person who claims he's the reincarnation of King Arthur either""",
410105: """SkaN: bought the cat a new litter box. the cat is thrilled
SkaN: it's so comfy to sleep in ]:->""",
410106: """xxx: Started cleaning.
ххх: Considering that household dust is 70% dead skin flakes, I just scooped a couple of corpses out of my computer case. Going to bury them.""",
410107: """dpi: I figured out how my brain works
dpi: 1) Look what awesome rakes, let's step on them!
dpi: 2) I'm telling you, these are new rakes, let's step on them!
dpi: 3) No, those were the same ones, but over there are new ones!
dpi: repeat to infinity...""",
410108: """*Шеф* > Who will explain to me why the proofreader, giggling, returned the draft manual with the comment "pet it"?
. . . .
*Шеф* > Which bastard replaced manual with manul throughout the entire manual a day before it went to print?""",
410109: """xxx: oh this end of the world
xxx: my grandma is pondering whether to plant tomatoes or not""",
410110: """xxx: I think I have schizophrenia. One part of me wants to sleep, another part of me wants to stay online a bit longer, and I have to suffer :(""",
410111: """Felis_Chaus: Calling and breathing into the receiver is no longer in fashion.
Felis_Chaus: Now you open a contact's window in ICQ and write nothing.""",
410112: """xxx: When did he ever have fun at a birthday party?
xxx: Oh wait, there was — when he got stuck in the elevator.""",
410113: """XXX: so what are your plans for August?
YYY: Napoleonic ones
ХХХ: decided to run from Russia?""",
410114: """xxx: my cacti don't grow, the cat kills them for some reason, gnaws them to death.
yyy: probably saving the mice""",
410115: """Habr.
xxx: As a rule, a person who feels like a professional couldn't care less about studying.
yyy: As a rule, draft offices couldn't care less about professionalism.""",
410116: """ххх: I got officially hired. They told me to bring my own mug.""",
410117: """Sakhalin.Info
In Chekhov, the employment service recommends the unemployed take up business.

Скрухет:
In Chekhov the employment service recommends the unemployed work
анонимный(17980):
The Ministry of Health recommends the sick get treatment, the hungry eat, and the unemployed work. Everyone's happy.""",
410118: """From InoSMI:
This should become a fine tradition.

Every American president should be awarded the Nobel Peace Prize right after inauguration, after which he should declare war on someone.""",
410119: """ххх: in the morning I put a status on VK: "Change! — our hearts demand!"
ххх: in the evening they brought a draft summons...""",
410120: """xxx: No, I marvel at you...
xxx: You read fairy tales, wish with all your heart for a unicorn. Found one. And after a while: its horn is too short, it doesn't shimmer with mother-of-pearl, it wobbles when riding, the color could be lighter, it burns too much magic dust per 100 km...
xxx: And in general, WHY ISN'T IT A PEGASUS???????""",
410121: """xxx: It's so sad when a stupid person thinks he's clever...""",
410122: """xxx: I'm a beginning programmer, with less than two years of experience, I'm not allowed more than seventy lines in one program (""",
410123: """Our accountant on the phone: You're reasoning from the standpoint of common sense, not the tax code.""",
410124: """Дашенька: How's spring? Has Cupid shot your sensitive heart yet?
Ирочка: Despite the fact that my ideal figure includes two, or even three equally ideal ones, Cupid still couldn't hit even my ass, let alone my heart. And since we're on the subject of pagan gods and my internal organs, I can say with confidence that Dionysus is a much better shot than Cupid, because he's managed to hit my liver with a shot of cognac more than once this month.""",
410125: """xxx: The Firefox developers have hit a dead end.
xxx: No matter what experiments they conduct, they always end up with Opera.""",
410126: """xxx: Smoking is bad!
yyy: what led you to this brilliant thought?
xxx: I had an epic fail yesterday. Scoured half of Moscow looking for mid-season boots. Went with a friend. Decided to buy myself cigarettes on the way, though my friend talked me out of it. I went to another mall alone. Found the boots right before closing... And what do you think, I was short exactly the price of a pack of cigarettes. Had to put it off.""",
410127: """xxx: What shall we name our kids?:)
yyy: depends on who's born.
ххх: Well, who do you want more?^^
yyy: a cat.""",
410128: """Котенок: Don't blow my mind, it's Monday even without you.""",
410129: """From Habr:
x: And what do you use to organize queues?
y: Grannies.""",
410130: """xxx: A sweater is that warm woolen clothing a child puts on when his mother is cold.""",
410131: """<колючий> hi everyone, does anyone have a video-editing program for newbies?
<Lokki> wait, can you even process video on kettles now?""",
410132: """xxxxx: My husband came home from work and demands to be fed. I wave him off:
- Wait, I'll reply to mom's email, she's asking what's new with us.
Husband, categorically:
- If you don't feed me immediately — you can safely reply that the news is criminal!""",
410133: """xxx: Russia has the best doctors. Especially at the pre-draft medical exam.
xxx: Only a Russian doctor, listening to a patient's chest, can confidently diagnose: "You have flat feet\"""",
410134: """xxx: someone rear-ended me yesterday and drove off fast
xxx: on my dirty bumper there's an imprint of the license plate""",
410135: """KermlinRussia: We're ready to forgive Gaddafi for being a dictator and a murderer, but gas at 14 cents a liter and a teacher's salary of $3k a month — that's already lawlessness.""",
410136: """xxx: Was sitting at the computer with my 10-month-old daughter.
xxx: Daughter showed me several hotkey combinations I never even suspected.""",
410137: """ххх: so, how was your first night?
yyy: he threw me on the bed and started gnawing my bra!""",
410138: """RomAndry:
An acquaintance recently received several broken iPhone 3Gs (for parts).
At customs they decided to "make some money" and blatantly billed him for customs clearance as if they were new iPhone 4Gs.

Without a word, he went to the bank, paid, took witnesses, and went to customs.
There, handing over the receipts and receiving the cargo, he protested: "Where are my new 4Gs?!"
There's an investigation going on. The acquaintance is getting new 4Gs or compensation.
He taught them a good lesson — a note for those who receive and clear customs.""",
410139: """Ирина М: went to see my academic advisor yesterday...
NEudavka: with vaseline?
Ирина М: Masha, we're psychologists: we slowly eat away at the brain, breeding guilt, not rudely barge into the inner world with claims!""",
410140: """ххх: Why is no one answering my resume? Already sent out a dozen!
ууу: Are you sending from the same mailbox you write to me from? You know, the one that signs all its letters "With respect, your Enorez Toksodermovich\"""",
410141: """xxx: how are you?
yyy: Spring, birds shining, sun chirping — an exacerbation, in general.""",
410142: """ххх: In your case, vegetarianism is cannibalism!""",
410143: """Rummaging through dad's tools looking for screws. I realize I can't make sense of all these little boxes and bags without a hint and shout:
- Mom, do you know where dad keeps his screws?
Suddenly one of the bags rips and all the screws spill on the floor with a characteristic sound.
Mom from the kitchen, cheerfully:
- I know! On the floor!""",
410144: """xxx: Modern new buildings can be compared to sausage.
xxx: Better not to know what they're made of and how.""",
410145: """My son went to a job interview (looking for his first job). An SMS arrives from him:
The HR woman read my resume and left with it toward the restroom... Should I better leave now? Or should I wait for her to come back?""",
410146: """"22:13, 22/03/2011: Putin dined in Ljubljana on dandelion salad and fish with morels."

RIA Novosti. Only the most important.""",
410147: """Somehow we're sitting with parents in the kitchen, discussing my student life. Mom says: your dad is great, he didn't drink before the army, didn't drink in college, and dad dreamily: "Right, Volodya didn't drink — Volodya was on a BENDER.\"""",
410148: """- Hello, I'd like to make an appointment with the ENT. Here's my referral.
- Your referral has an expiry date. When did you apply?
- I applied a month and a half ago, but the ENT was on sick leave.
- What, did the need for treatment go away in that time?
(c) City Polyclinic No. 2, Novosibirsk, Lazurnaya St.""",
410149: """xxx: I feel like my cat was assembled at a Lego factory.
xxx: She fills all the free space under the blanket so well.""",
410150: """I come to the director, he's yelling that I should "fix the internet". His mail.ru won't open. Well, I fiddle around... pings to other servers go through. Other sites open. I got to thinking. On another computer — the same. Remotely logged into my home one, checked what's there. Same.
Говорю: Excuse me, you do have internet, it's the mail.ru server that's down.
директор: Why doesn't it work on yours? Go fix it.
Я: It's in Moscow, we're in Yaroslavl. I think by the time I get there, they'll have fixed it themselves.
Директор: But isn't the whole internet on your server? Then why did we buy it?

I somehow explained to the director how the internet works and what a gateway with a firewall and DNS is.""",
410151: """(Dialogue between the executive director and a girl from the HR department)
ДиОпП: I had something in my head
ИД: a brain?
ДиОпП: nooo! something important...""",
410152: """xxx: Is the Svetresurs 333-005-03 floor lamp in stock?
магазин: Please introduce yourself.
xxx: Alexander
магазин: Hello, Alexander. The floor lamp is not in stock.
xxx: hmm, what if I'm Igor?""",
410153: """from a forum, topic about divorce

xxx: a woman came to our work once, ordered fireworks for 15 thousand, asked to
set them off on the central square. when asked the reason for the celebration, she answered — divorce""",
410154: """xxx: Today I opened the case to look after power surges, looks like the PSU burned out.
xxx: I open it, everything is covered in dust and there's an inscription in the dust "Zhenya was here. 2008.\"""",
410155: """xxx: Only the lack of explored oil reserves in Antarctica holds penguins back from revolution.""",
410156: """ххх: I specially put on a dress and waited for you on Skype to show it. And you didn't come
ууу: I feel ashamed now :(
ххх: oh come on, whatever
ххх: don't worry
ххх: there are worse things
ххх: one boyfriend of mine dumped me while I was pregnant once. That was offensive, yeah. But you not showing up on Skype is nothing.""",
410157: """хxx: Where you fell at night — that's where the planetarium is (c) :D
уyy: and if you fell during the day?
zzz: Lie down, wait for it to open.""",
410158: """FoxMulder: Riding the bus in the morning. Crowd, Monday, nerves. And the driver naturally rushes forward at speed. People grumble "You're not hauling firewood!"
A drunk at the back seat, lifting his head: "No, he IS hauling firewood!\"""",
410159: """Ta-dam: Standing at the bus stop at ZhBI (a tough-guy neighborhood). Waiting for the bus, a kid comes from deep in the neighborhood, in an Adidas jacket, jeans, a cap, with a gym bag, also stands to wait for the bus. It arrives, we get on. Riding, we leave the neighborhood, start approaching the center, and then he takes off his jacket, cap, tucks his jeans, as it turned out, into tall Nikes, takes a short shiny jacket out of the bag, big glasses, puts on a watch, and puts all the "tough-guy" stuff in the bag.""",
410160: """There's a photo studio on our floor, and accordingly they process photos in Photoshop. Now washing my hands in the restroom, from the stall comes a phone conversation "... yes, yes, right now I'm looking at my creation, it turned out beautifully, you'll like it...\"""",
410161: """xxx: From the neighbors' balcony a mouse ran into the apartment
xxx: Discovered it, meeting its gaze while cleaning the closet
xxx: Now I live in the hallway and finally want to get married""",
410162: """[Ecler] I AM SLOWLY TAKING OFF YOUR PANTIES
[Ubagumov] ya medlenno dobavlyaiy tebua v ignore""",
410163: """xxx: Once Christ drove the merchants out of the temple.
xxx: Since then the merchants got smarter and bought temples for themselves.""",
410164: """Extract of cashmere gives hair softness.
Extract of bamboo gives hair elasticity.
Extract of shoe polish gives hair shine.
And extract of styrofoam gives lightness.""",
410165: """yyy: as a kid I thought brake fluid was rough and poured under the wheels when braking )""",
410166: """23.03.2011 10:18:42, _EL
It was a mistake to sneeze at the boss's phrase "What, am I an IDIOT?" while he was arguing with the client on the phone...""",
410167: """xxx: Turns out, humanity's first message to our brothers in mind consisted of three words: "Peace. Lenin. USSR."
xxx: Imagine, dear earthlings, that you receive a message from space: "Peace. Overlord. Galactic Empire."
xxx: Would you answer such a thing or would you get worried?""",
410168: """xxx: I'm playing, grandma comes in and says: "go eat."
xxx: I pause, go to the kitchen, don't find the promised food, come back, and she's already sitting there playing solitaire...""",
410169: """xxx: Why don't the owners of the "Love is" brand start making condoms?
xxx: The kids of the 90s have already grown up.""",
410170: """[Шкаф] btw, why do girls get offended at random bullshit?
[Шкаф] once one asked me — which star do I look like?
[Шкаф] I say — Kipelov
[Шкаф] pfft, that's it, friend became enemy...""",
410171: """In the days of happy Soviet childhood I had a doll. Her name was Ada. That's what it said on the label — Doll Ada. No clothes fit her and she kept breaking. And only many years later it dawned on me...""",
410172: """xxx: people used to give birth quite soon after the wedding, it was a custom -)
yyy: it's still a custom now :) about 5-6 months later :)""",
410173: """From a forum:
glonass isn't so that you know where the country is, but so that the country knows where you )""",
410174: """Article by V.V. Putin "Russia must not be and will not be a police state" // Izvestia - 2000. - July 14""",
410175: """xxx: Darling, shall we play role-playing games?
xxx: You're the cleaning lady, and I'm asleep.""",
410176: """SJA: All my wife's fears and worries can be found in her Google search history.""",
410177: """xxx: Woke up thinking: exams are coming soon. Got upset.
xxx: Then remembered there are no exams, I have to go to work.
xxx: Got even more upset.""",
}


def main():
    inp = '/home/hatch/workspace/translation-work/bashim/chunks/chunk_28.jsonl'
    outp = '/home/hatch/workspace/translation-work/bashim/out/chunk_28.jsonl'
    recs = [json.loads(l) for l in open(inp)]
    batch = recs[0:100]
    want = {r['id'] for r in batch}
    assert set(EN) == want, 'key mismatch: %s' % (set(EN) ^ want)
    with open(outp, 'a') as f:
        for r in batch:
            en = EN[r['id']]
            ru = r['text']
            assert en, r['id']
            assert en.count('\n') == ru.count('\n'), 'newline mismatch %s: en=%d ru=%d' % (r['id'], en.count('\n'), ru.count('\n'))
            f.write(json.dumps({'id': r['id'], 'ru': ru, 'en': en}, ensure_ascii=False) + '\n')
    print('batch28_1 OK, wrote', len(batch))


if __name__ == '__main__':
    main()
